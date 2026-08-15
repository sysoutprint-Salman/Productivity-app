package SpringBoot;

import lombok.RequiredArgsConstructor;
import notebook.Notebook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/notebooks")
@RequiredArgsConstructor
public class NotebookController {
    private final NotebookRepository notebookRepository;
    @GetMapping
    protected List<Notebook> getAllNotebooks(){
        return notebookRepository.getAllNotebooks();
    }
    @GetMapping("/{id}")
    protected Notebook getNotebook(Long id){
        Notebook notebook = notebookRepository.getNotebook(id);
        if (notebook != null) return notebook;
        else System.out.println("SpringBoot: Notebook not found. Returning empty Notebook.");
        return new Notebook();
    }
    @GetMapping("/filter")
    protected List<Notebook> findByUserId(@RequestParam Long userId){
        return notebookRepository.findByUserId(userId);
    }

    @PostMapping
    protected ResponseEntity<?> postNotebook(@RequestBody Notebook notebook){
        notebookRepository.postNotebook(notebook);
        return ResponseEntity.ok("SpringBoot: Notebook successfully created.");
    }
    @PutMapping("/{id}/tab")
    protected ResponseEntity<?> updateNotebookTab(@PathVariable Long id, @RequestBody Notebook notebook){
            Notebook existingNotebook = notebookRepository.getNotebook(id);
            if (existingNotebook == null) return ResponseEntity.notFound().build();
            existingNotebook.setTabTitle(notebook.getTabTitle());
            notebookRepository.updateNotebookTab(id, existingNotebook);
            return ResponseEntity.ok("SpringBoot: Tab updated successfully");
    }
    //PUT implementation for the notepad auto-saving
    @PutMapping("/{id}/text")
    protected ResponseEntity<?> updateNotebookText(@PathVariable Long id, @RequestBody Notebook notebook){
        Notebook existingNotebook = notebookRepository.getNotebook(id);
        if (existingNotebook == null) return ResponseEntity.notFound().build();
        existingNotebook.setNotebookText(notebook.getNotebookText());
        notebookRepository.updateNotebookText(id, existingNotebook);
        return ResponseEntity.ok("SpringBoot: Notebook text updated successfully");
    }

    @DeleteMapping("/{id}")
    protected ResponseEntity<?> deleteNotebook(@PathVariable Long id){
        notebookRepository.deleteNotebook(id);
        return ResponseEntity.ok("SpringBoot: Notebook deleted successfully.");
    }
}
@Repository
class NotebookRepository {
    @Autowired
    private JdbcTemplate jdbc;
    private final NotebookMapper notebookMapper = new NotebookMapper();

    protected List<Notebook> getAllNotebooks(){
        return jdbc.query("SELECT * FROM notebooks",notebookMapper);
    }
    protected List<Notebook> findByUserId(Long user_id){
        return jdbc.query("SELECT * FROM notebooks WHERE user_id = ?"
                ,notebookMapper, user_id);
    }
    protected Notebook getNotebook(Long id){
        return jdbc.queryForObject("SELECT * FROM notebooks WHERE id = ?",
               notebookMapper ,id);
    }
    protected void postNotebook(Notebook notebook){
        jdbc.update("INSERT INTO notebooks (tab_title, notebook_text, user_id, hex_color) VALUES (?, ?, ?, ?)",
                notebook.getTabTitle(),
                notebook.getNotebookText(),
                notebook.getUserId(),
                notebook.getHexColor());
    }
    public void updateNotebookText(Long id, Notebook notebook) {
        jdbc.update("UPDATE notebooks SET notebook_text = ? WHERE id = ?",
                notebook.getNotebookText(), id);
    }
    public void updateNotebookTab(Long id, Notebook notebook) {
        jdbc.update("UPDATE notebooks SET tab_title = ?, hex_color = ? WHERE id = ?",
                notebook.getTabTitle(), notebook.getHexColor(), id);
    }

    protected void deleteNotebook(Long id){
        jdbc.update("DELETE FROM notebooks WHERE id = ?", id);
    }
}
class NotebookMapper implements RowMapper<Notebook>{

    @Override
    public Notebook mapRow(ResultSet rs, int rowNum) throws SQLException {
        Notebook notebook = new Notebook();
        notebook.setNotebookId(rs.getLong("id"));
        notebook.setUserId(rs.getLong("user_id"));
        notebook.setTabTitle(rs.getString("tab_title"));
        notebook.setNotebookText(rs.getString("notebook_text"));
        notebook.setHexColor(rs.getString("hex_color"));
        return notebook;
    }
}
