package top.clematis.aquanote.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.clematis.aquanote.dto.SyncRequest;
import top.clematis.aquanote.dto.SyncResponse;
import top.clematis.aquanote.mapper.NoteMapper;
import top.clematis.aquanote.pojo.Note;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteSyncService {

    private final NoteMapper noteMapper;

    //处理客户端同步请求
    @Transactional
    public SyncResponse syncNotes(String userId, SyncRequest syncRequest) {
        try {
            //获取服务器端自上次同步以来的变更
            List<Note> serverChanges = noteMapper.getNotesAfterVersion(userId, syncRequest.getLastSyncVersion());
//            System.out.println(serverChanges);
            //处理客户端提交的变更
            List<SyncResponse.ConflictNote> conflicts = new ArrayList<>();
            List<Note> successfulUpdates = new ArrayList<>();

            for (Note clientNote : syncRequest.getLocalChanges()) {
                try {
                    processClientNote(userId, clientNote, conflicts, successfulUpdates);
                } catch (Exception e) {
                    log.error("处理客户端笔记失败: {}", clientNote.getNoteId(), e);
                }
            }

            Long currentSyncVersion = noteMapper.getMaxSyncVersion(userId);
            if (currentSyncVersion == null) {
                currentSyncVersion = 0L;
            }

            SyncResponse response = new SyncResponse();
            response.setCurrentSyncVersion(currentSyncVersion);
            response.setServerChanges(serverChanges);
            response.setConflicts(conflicts);
            response.setSuccess(true);
            response.setMessage("同步成功");
            return response;

        } catch (Exception e) {
            log.error("同步过程发生错误", e);
            SyncResponse errorResponse = new SyncResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("同步失败: " + e.getMessage());
            return errorResponse;
        }
    }


    //处理单个客户端笔记
    private void processClientNote(String userId, Note clientNote,
                                   List<SyncResponse.ConflictNote> conflicts,
                                   List<Note> successfulUpdates) {

        Note serverNote = noteMapper.getNoteById(clientNote.getNoteId());
//        System.out.println("serve: "+serverNote);
//        System.out.println("client:"+clientNote);
        if (serverNote == null) {
            createNewNote(userId, clientNote);
            successfulUpdates.add(clientNote);
        } else {
            // 检查是否冲突
            if (serverNote.getSyncVersion().equals(clientNote.getSyncVersion())) {
                updateExistingNote(clientNote);
                successfulUpdates.add(clientNote);
            } else {
                // 版本冲突，记录冲突信息
                SyncResponse.ConflictNote conflict = new SyncResponse.ConflictNote();
                conflict.setServerVersion(serverNote);
                conflict.setClientVersion(clientNote);
                conflict.setConflictReason("版本冲突：服务器版本=" + serverNote.getSyncVersion() +
                        ", 客户端版本=" + clientNote.getSyncVersion());
                conflicts.add(conflict);
            }
        }
    }

    /**
     * 创建新笔记
     */
    private void createNewNote(String userId, Note note) {
        if (note.getNoteId() == null) {
            note.setNoteId(UUID.randomUUID().toString());
        }
        note.setUserId(userId);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        note.setSyncVersion(generateNextSyncVersion(userId));

        noteMapper.insertNote(note);
    }
    /**
     * 更新现有笔记
     */
    private void updateExistingNote(Note note) {
        // 保存原始版本号用于乐观锁检查
        Long originalVersion = note.getSyncVersion();
        
        note.setUpdatedAt(LocalDateTime.now());
        note.setSyncVersion(generateNextSyncVersion(note.getUserId()));

        int updatedRows = noteMapper.updateNoteWithVersionCheck(note, originalVersion);
//        System.out.println(note);
        if (updatedRows == 0) {
            throw new RuntimeException("乐观锁更新失败，笔记可能已被其他客户端修改");
        }
    }

    /**
     * 生成下一个同步版本号
     */
    private Long generateNextSyncVersion(String userId) {
        Long maxVersion = noteMapper.getMaxSyncVersion(userId);
        return (maxVersion == null ? 0L : maxVersion) + 1;
    }

    /**
     * 强制解决冲突
     * 未测试和未对接
     */
    @Transactional
    public SyncResponse resolveConflictWithServerVersion(String userId, String noteId) {
        Note serverNote = noteMapper.getNoteById(noteId);
        if (serverNote == null || !serverNote.getUserId().equals(userId)) {
            SyncResponse errorResponse = new SyncResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("笔记不存在或无权限");
            return errorResponse;
        }

        SyncResponse response = new SyncResponse();
        response.setCurrentSyncVersion(serverNote.getSyncVersion());
        response.setServerChanges(List.of(serverNote));
        response.setSuccess(true);
        response.setMessage("冲突已解决，采用服务器版本");
        return response;
    }

    /**
     * 强制解决冲突
     * 未测试和未对接
     */
    @Transactional
    public SyncResponse resolveConflictWithClientVersion(String userId, Note clientNote) {
        clientNote.setUserId(userId);
        updateExistingNote(clientNote);

        SyncResponse response = new SyncResponse();
        response.setCurrentSyncVersion(clientNote.getSyncVersion());
        response.setSuccess(true);
        response.setMessage("冲突已解决，采用客户端版本");
        return response;
    }
}
