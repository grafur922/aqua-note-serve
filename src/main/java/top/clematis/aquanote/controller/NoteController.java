package top.clematis.aquanote.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import top.clematis.aquanote.config.AquaSecurityProperties;
import top.clematis.aquanote.dto.ApiResponse;
import top.clematis.aquanote.dto.SyncRequest;
import top.clematis.aquanote.dto.SyncResponse;
import top.clematis.aquanote.pojo.Note;
import top.clematis.aquanote.pojo.Tag;
import top.clematis.aquanote.service.NoteSyncService;
import top.clematis.aquanote.mapper.NoteMapper;
import top.clematis.aquanote.util.SecurityUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NoteController {

    private static final Logger log = LoggerFactory.getLogger(NoteController.class);
    private final NoteSyncService noteSyncService;
    private final NoteMapper noteMapper;
    private final AquaSecurityProperties securityProperties;

    private <T> ResponseEntity<ApiResponse<T>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(401, "未认证"));
    }

    private String resolveUserId(Jwt jwt, String userIdHeader) {
        return SecurityUtils.resolveUserId(jwt, userIdHeader, securityProperties.isDebugAllowUserIdHeader());
    }

    /**
     * 同步笔记
     * POST /api/sync
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<SyncResponse>> syncNotes(
            @RequestHeader(value = "User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SyncRequest syncRequest) {

        String userId = resolveUserId(jwt, userIdHeader);
        if (userId == null) {
            return unauthorized();
        }
        
        SyncResponse syncResponse = noteSyncService.syncNotes(userId, syncRequest);
//        System.out.println(syncResponse);
        if (syncResponse.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success( "同步成功",syncResponse));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(syncResponse.getMessage()));
        }
    }
    
    //暂未对接
    @PostMapping("/{noteId}/resolve-conflict/server")
    public ResponseEntity<ApiResponse<SyncResponse>> resolveConflictWithServer(
            @RequestHeader(value = "User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String noteId) {

        String userId = resolveUserId(jwt, userIdHeader);
        if (userId == null) {
            return unauthorized();
        }
        
        SyncResponse response = noteSyncService.resolveConflictWithServerVersion(userId, noteId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success( "冲突已解决",response));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(response.getMessage()));
        }
    }
    
    //暂未对接
    @PostMapping("/resolve-conflict/client")
    public ResponseEntity<ApiResponse<SyncResponse>> resolveConflictWithClient(
            @RequestHeader(value = "User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Note clientNote) {

        String userId = resolveUserId(jwt, userIdHeader);
        if (userId == null) {
            return unauthorized();
        }
        
        SyncResponse response = noteSyncService.resolveConflictWithClientVersion(userId, clientNote);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success( "冲突已解决",response));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(response.getMessage()));
        }
    }
    
    /**
     * 获取用户所有笔记
     * Post /api/notes
     */
    @PostMapping
    public ResponseEntity<ApiResponse<List<Note>>> getUserNotes(
            @RequestHeader(value = "User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String,Integer> payLoad){
        String userId = resolveUserId(jwt, userIdHeader);
        if (userId == null) {
            return unauthorized();
        }
        Integer tagId=payLoad.get("tagId");
        List<Note> notes;
        if(tagId==null){
            notes= noteMapper.getUserNotes(userId);
        }
        else{
            notes = noteMapper.getUserNotesByTag(userId,tagId);
        }

        return ResponseEntity.ok(ApiResponse.success( "获取笔记列表成功",notes));
    }

    /**
     * 搜索笔记
     * GET /api/notes/search?keyword=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Note>>> searchNotes(
            @RequestHeader(value = "User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String keyword) {

        String userId = resolveUserId(jwt, userIdHeader);
        if (userId == null) {
            return unauthorized();
        }
        
        List<Note> notes = noteMapper.searchNotes(userId, keyword);
        return ResponseEntity.ok(ApiResponse.success( "搜索完成",notes));
    }
    
    /**
     * 获取单个笔记
     * GET /api/notes/{noteId}
     */
    @GetMapping("/{noteId}")
    public ResponseEntity<ApiResponse<Note>> getNote(
            @RequestHeader(value = "User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String noteId) {

        String userId = resolveUserId(jwt, userIdHeader);
        if (userId == null) {
            return unauthorized();
        }
        
        Note note = noteMapper.getNoteById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ApiResponse.success( "获取笔记成功",note));
    }
    
    /**
     * 软删除笔记
     * DELETE /api/notes/{noteId}
     */
    @DeleteMapping("/{noteId}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @RequestHeader(value = "User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String noteId) {

        String userId = resolveUserId(jwt, userIdHeader);
        if (userId == null) {
            return unauthorized();
        }
        
        int result = noteMapper.softDeleteNote(noteId, userId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success( "删除成功",null));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取tag
     * GET /api/notes/tags
     */
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<Tag>>> getUserTags(
            @RequestHeader(value = "User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal Jwt jwt
    ){
        String userId = resolveUserId(jwt, userIdHeader);
        if (userId == null) {
            return unauthorized();
        }
        List<Tag> tags = noteMapper.getUserTags(userId);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 新增Tag
     * POST /api/notes/addTag
     */
    @PostMapping("/addTag")
    public ResponseEntity<ApiResponse<Integer>> addUserTags(
            @RequestHeader(value = "User-Id", required = false) String userIdHeader,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String,String> tagName
    ){
        String userId = resolveUserId(jwt, userIdHeader);
        if (userId == null) {
            return unauthorized();
        }
        try {
            Integer tagId = noteMapper.addUserTag(userId, tagName.get("name"));
            if(tagId==null){
                return ResponseEntity.ok(ApiResponse.error("tagName exist"));
            }

        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("tagName exist"));
        }
        return ResponseEntity.ok(ApiResponse.success("添加成功",null));
    }
}
