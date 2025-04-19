package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExcelViewModel {
    private String gtip;
    private String grossWeigth;
    private String netWeigth;
    private String origin;
    private String productCode;
    private String productName;
    private String quantity;
    private String unitPrice;
    private String totalAmount;
}
