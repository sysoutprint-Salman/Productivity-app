package notebook;
import org.junit.Test;



import java.util.List;


import static org.testng.AssertJUnit.*;

public class NotebookTest {

    private final NotebookService notebookService = new NotebookService();



    private Notebook createTestNotebook(long userId, String title, String text, String color) {
        Notebook notebook = new Notebook();
        notebook.setUserId(userId);
        notebook.setTabTitle(title);
        notebook.setNotebookText(text);
        notebook.setHexColor(color);

        return notebook;
    }

    @Test
    public void createNotebook() {
        Notebook notebook = createTestNotebook(
                2L,
                "My Notes",
                "This is my notebook.",
                "#FFFFFF"
        );

        notebookService.createNotebook(notebook);

        List<Notebook> notebooks =
                notebookService.findByUserId(2L);

        assertEquals("My Notes", notebooks.getLast().getTabTitle());
        assertEquals("This is my notebook.", notebooks.getLast().getNotebookText());
        assertEquals("#FFFFFF", notebooks.getLast().getHexColor());
    }

    @Test
    public void getNotebook() {
        Notebook notebook = createTestNotebook(
                2L,
                "My Notes",
                "Some notes",
                "#FFFFFF"
        );
        System.out.println("Previous: " + notebook);
        notebookService.createNotebook(notebook);

        Notebook result = notebookService.getNotebook(notebook.getNotebookId());
        System.out.println("Fetched: " + notebook);
        assertEquals("My Notes", result.getTabTitle());
        assertEquals("Some notes", result.getNotebookText());
    }

    @Test
    public void findByUserId() {
        Notebook notebook = createTestNotebook(
                2L,
                "This notebook123",
                "Some notes",
                "#FFFFFF"
        );
        notebookService.createNotebook(notebook);

        assertEquals(notebook,notebookService.findByUserId(2L).getLast());

    }

    @Test
    public void updateNotebookText() {
        Notebook notebook = createTestNotebook(
                2L,
                "My Notes",
                "Original text",
                "#FFFFFF"
        );

        notebookService.createNotebook(notebook);

        notebook.setNotebookText("Updated text");

        assertTrue(notebookService.updateNotebookText(notebook.getNotebookId(), notebook));

        Notebook updated = notebookService.getNotebook(notebook.getNotebookId());

        assertEquals("Updated text", updated.getNotebookText());
    }

    @Test
    public void updateNotebookTab() {
        Notebook notebook = createTestNotebook(
                2L,
                "Original Title",
                "Some text",
                "#FFFFFF"
        );

        notebookService.createNotebook(notebook);

        notebook.setTabTitle("New Title");
        notebook.setHexColor("#000000");

        assertTrue(
                notebookService.updateNotebookTab(
                        notebook.getNotebookId(),
                        notebook
                )
        );

        Notebook updated =
                notebookService.getNotebook(notebook.getNotebookId());

        assertEquals("New Title", updated.getTabTitle());
        assertEquals("#000000", updated.getHexColor());
    }

    @Test
    public void deleteNotebook() {
        Notebook notebook = createTestNotebook(
                2L,
                "My Notes",
                "Some notes",
                "#FFFFFF"
        );

        notebookService.createNotebook(notebook);

        assertTrue(
                notebookService.deleteNotebook(
                        notebook.getNotebookId()
                )
        );

    }
}
