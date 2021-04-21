package ch.xavier.tradingbot.realtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


class RealtimeQuotesImporterTest {
    private RealtimeQuotesImporter importer;

    @BeforeEach
    public void setUp() {
        importer = new RealtimeQuotesImporter();
    }


    @Test
    @Disabled
    public void watchFB() throws InterruptedException {
        //US markets are closed during your day...
        importer.watchSymbol("FB");

        Thread.sleep(20000);
    }
}