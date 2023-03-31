
public class EventMainMenuRender implements Event {
	public fu GuiMainMenu;
	public sj FontRenderer;
	public Boolean isBefore;
	
	public EventMainMenuRender(fu GuiMainMenu, sj FontRenderer, boolean isBefore)
	{
		this.GuiMainMenu = GuiMainMenu;
		this.FontRenderer = FontRenderer;
		this.isBefore = isBefore;
	}
	
	public Boolean isBefore() {
		return isBefore;
	}
	public Boolean isAfter() {
		return !isBefore;
	}
	
	public String getEventID() {
		return "EventMainMenuRender";
	}
	
	public void renderString(String text, int x, int y, int color)
	{
		this.GuiMainMenu.b(this.FontRenderer, text, x, y, color);
	}
	
	public void renderString(sj fontRenderer, String text, int x, int y, int color)
	{
		this.GuiMainMenu.b(fontRenderer, text, x, y, color);
	}
}
