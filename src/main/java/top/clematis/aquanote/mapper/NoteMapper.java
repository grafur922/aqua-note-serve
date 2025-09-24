package top.clematis.aquanote.mapper;

import org.apache.ibatis.annotations.*;
import top.clematis.aquanote.pojo.Note;

import java.util.List;

@Mapper
public interface NoteMapper {
    
    @Select("SELECT * FROM Note WHERE user_id = #{userId} AND sync_version > #{lastSyncVersion} ORDER BY sync_version")
    List<Note> getNotesAfterVersion(@Param("userId") String userId, @Param("lastSyncVersion") Long lastSyncVersion);
    
    @Select("SELECT * FROM Note WHERE note_id = #{noteId}")
    Note getNoteById(@Param("noteId") String noteId);
    
    @Select("SELECT MAX(sync_version) FROM Note WHERE user_id = #{userId}")
    Long getMaxSyncVersion(@Param("userId") String userId);
    
    @Insert("INSERT INTO Note (note_id, user_id, title, content, is_archived, is_deleted, created_at, updated_at, sync_version) " +
            "VALUES (#{noteId}, #{userId}, #{title}, #{content}, #{isArchived}, #{isDeleted}, #{createdAt}, #{updatedAt}, #{syncVersion})")
    int insertNote(Note note);

    @Update("UPDATE Note SET title = #{note.title}, content = #{note.content}, is_archived = #{note.isArchived}, " +
            "is_deleted = #{note.isDeleted}, updated_at = #{note.updatedAt}, sync_version = #{note.syncVersion} " +
            "WHERE note_id = #{note.noteId} AND sync_version = #{originalVersion}")
    int updateNoteWithVersionCheck(@Param("note") Note note, @Param("originalVersion") Long originalVersion);
    
    @Select("SELECT * FROM Note WHERE user_id = #{userId} AND is_deleted = false ORDER BY updated_at DESC")
    List<Note> getUserNotes(@Param("userId") String userId);
    
    @Select("SELECT * FROM Note WHERE user_id = #{userId} AND is_deleted = false AND " +
            "(title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))")
    List<Note> searchNotes(@Param("userId") String userId, @Param("keyword") String keyword);
    
    @Update("UPDATE Note SET is_deleted = true, updated_at = NOW(), sync_version = sync_version + 1 " +
            "WHERE note_id = #{noteId} AND user_id = #{userId}")
    int softDeleteNote(@Param("noteId") String noteId, @Param("userId") String userId);
}
