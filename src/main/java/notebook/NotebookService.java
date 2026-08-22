package notebook;

import java.util.List;

public class NotebookService {

    private final NotebookRepo notebookRepo;

    public NotebookService() {
        notebookRepo = new NotebookRepo();
    }

    public List<Notebook> getAllNotebooks() {
        return notebookRepo.getAllNotebooks();
    }

    public Notebook getNotebook(Long id) {
        Notebook notebook = notebookRepo.getNotebook(id);

        if (notebook != null) {
            return notebook;
        }

        System.out.println("Notebook not found. Returning empty Notebook.");
        return new Notebook();
    }

    public List<Notebook> findByUserId(Long userId) {
        return notebookRepo.findByUserId(userId);
    }

    public Notebook createNotebook(Notebook notebook) {
        return notebookRepo.createNotebook(notebook);
    }

    public boolean updateNotebookTab(Long id, Notebook notebook) {
        Notebook existingNotebook = notebookRepo.getNotebook(id);

        if (existingNotebook == null) {
            return false;
        }

        existingNotebook.setTabTitle(notebook.getTabTitle());
        existingNotebook.setHexColor(notebook.getHexColor());

        notebookRepo.updateNotebookTab(id, existingNotebook);

        return true;
    }

    public boolean updateNotebookText(Long id, Notebook notebook) {
        Notebook existingNotebook = notebookRepo.getNotebook(id);

        if (existingNotebook == null) {
            return false;
        }

        existingNotebook.setNotebookText(notebook.getNotebookText());

        notebookRepo.updateNotebookText(id, existingNotebook);

        return true;
    }

    public boolean deleteNotebook(Long id) {
        Notebook existingNotebook = notebookRepo.getNotebook(id);

        if (existingNotebook == null) {
            return false;
        }

        notebookRepo.deleteNotebook(id);

        return true;
    }
}
