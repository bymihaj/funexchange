package bymihaj;

public enum Instrument {
	
	STKMON(Symbol.STK, Symbol.MON);
	
	private final Symbol primary;
	private final Symbol secondary;
	
	private Instrument(Symbol primary, Symbol secondary) {
		this.primary = primary;
		this.secondary = secondary;
	}
	
	public Symbol getPrimary() {
		return primary;
	}
	
	public Symbol getSecondary() {
		return secondary;
	}
	
}
