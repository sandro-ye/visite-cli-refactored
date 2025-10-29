package it.unibs.visite;

import it.unibs.visite.core.DomainException;
import it.unibs.visite.persistence.FilePersistence;
import it.unibs.visite.service.ConfigService;
import it.unibs.visite.service.RegimeService;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

public class RegimeServiceTest {
    
    private RegimeService regime;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDir = Files.createTempDirectory("visite-preclusioni");
        ConfigService config = new ConfigService(new FilePersistence(tempDir));
        regime = new RegimeService(config);
    }

    @Test
    void testPreclusioneFuoriMese() {
        YearMonth wrongMonth = YearMonth.now().plusMonths(2);
        assertThrows(DomainException.class, () -> 
            regime.addPreclusioneForMonth(wrongMonth, LocalDate.now().plusMonths(2).withDayOfMonth(5)));
    }

}
