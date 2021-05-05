package ch.xavier.quotes.importer.finnhub;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class FinnhubCryptoSymbol {
    private String description;
    private String displaySymbol;
    private String symbol;
}
