package ch.xavier.quotes.importer.config;

import reactor.core.publisher.Flux;

public final class GeneralConfig {

    private GeneralConfig() { }

    /**
     * From https://www.marketwatch.com/tools/screener?exchange=Nyse&report=MostActive
     * Most traded US symbols to backtest your strats
     * Here are 82 symbols
     */
    public static final Flux<String> MOST_TRADED_US_SYMBOLS = Flux.just("FB", "AAPL", "ZM", "SNAP", "PENN", "CCL",  //Mostly NYSE
            "KSS", "RIGL", "NIO", "WFC", "F", "SPY", "GE", "BAC", "DNR", "JE", "AZN", "NCLH",
            "SLV", "XLF", "ABEV", "PFE", "T", "NOK", "SPXS", "GDX", "HAL", "UVXY", "BA", "CBL", "C", "EWZ",

            "IBIO", "DPW", "OGEN", "AIM", "ZOM", "ATNM", "NAK", "UAVS", "AMPE", "TRXC", "NGD", "BTG", "GPL",  //Most traded AMEX
            "PTN", "NBY", "AXU", "PLM", "APT", "GSAT", "SVM", "MTNB", "AUMN", "UUUU", "NOG", "SENS",

            "TNXP", "HTBX", "SQQQ", "NBL", "MRNA", "ONTX", "SRNE", "IMRN", "AAL", "TOPS", "AMD", "BNGO", "QQQ", //Most traded NASDAQ
            "OPK", "NKLA", "CTRM", "MSFT", "UAL", "NOVN", "VBIV", "GLBS", "TQQQ", "VXRT", "WKHS", "ADMP");

    public static final Flux<String> TWO_OFTEN_TRADED_SYMBOLS = Flux.just("MSFT", "FB");
}
