package net.eggify;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateCheckerTest {
    @Test
    void selectsOnlyTheReleaseForTheRawSnapshotId() throws Exception {
        JsonArray versions = JsonParser.parseString("""
            [
              {"version_number":"2.1.1","version_type":"release","date_published":"2026-10-30T00:00:00Z",
               "loaders":["fabric"],"game_versions":["26.4-snapshot-6"]},
              {"version_number":"9.0.0","version_type":"release","date_published":"2026-10-31T00:00:00Z",
               "loaders":["fabric"],"game_versions":["26.4-alpha.6"]}
            ]
            """).getAsJsonArray();
        Method find = UpdateChecker.class.getDeclaredMethod("findLatestVersion", JsonArray.class, String.class);
        find.setAccessible(true);
        Optional<?> selected = (Optional<?>) find.invoke(null, versions, "26.4-snapshot-6");
        assertTrue(selected.isPresent());
        Method number = selected.orElseThrow().getClass().getDeclaredMethod("versionNumber");
        number.setAccessible(true);
        assertEquals("2.1.1", number.invoke(selected.orElseThrow()));
        assertTrue(((Optional<?>) find.invoke(null, versions, "26.3")).isEmpty());
    }
}
