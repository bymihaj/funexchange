package bymihaj.data.order;

import bymihaj.Instrument;

public class MarketOrderRequest extends AbstractOrder {
    
    private double amount;
    private Instrument instrument;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

}
