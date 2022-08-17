package me.MrGraycat.eGlow.GUI.Menus.Custom.Command;

public class CommandComponentHolder {

    private final CommandComponent component;
    private final String message;

    public CommandComponentHolder(CommandComponent component, String message){
        this.component = component;
        this.message = message;
    }

    public CommandComponent getComponent() {
        return component;
    }

    public String getMessage() {
        return message;
    }
}
