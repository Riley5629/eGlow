package me.MrGraycat.eGlow.Util.Packets.Chat;

import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;
import org.json.simple.JSONObject;

public class ChatModifier {
    private TextColor color;

    private Boolean bold;
    private Boolean italic;
    private Boolean underlined;
    private Boolean strikethrough;
    private Boolean obfuscated;

    private String font;

    private ProtocolVersion targetVersion;

    public ChatModifier() {
    }

    public ChatModifier(ChatModifier modifier) {
        Preconditions.checkNotNull(modifier, "modifier");
        this.color = modifier.color == null ? null : new TextColor(modifier.color);
        this.bold = modifier.bold;
        this.italic = modifier.italic;
        this.underlined = modifier.underlined;
        this.strikethrough = modifier.strikethrough;
        this.obfuscated = modifier.obfuscated;
        this.font = modifier.font;
        this.targetVersion = modifier.targetVersion;
    }

    public TextColor getColor() {
        return color;
    }

    public void setColor(TextColor color) {
        this.color = color;
    }

    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    public void setItalic(Boolean italic) {
        this.italic = italic;
    }

    public void setUnderlined(Boolean underlined) {
        this.underlined = underlined;
    }

    public void setStrikethrough(Boolean strikethrough) {
        this.strikethrough = strikethrough;
    }

    public void setObfuscated(Boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    /**
     * Returns true if bold is defined and set to true, false otherwise
     *
     * @return  true if bold is defined and set to true, false otherwise
     */
    public boolean isBold(){
        return Boolean.TRUE.equals(bold);
    }

    /**
     * Returns true if italic is defined and set to true, false otherwise
     *
     * @return  true if italic is defined and set to true, false otherwise
     */
    public boolean isItalic(){
        return Boolean.TRUE.equals(italic);
    }

    /**
     * Returns true if underlined is defined and set to true, false otherwise
     *
     * @return  true if underlined is defined and set to true, false otherwise
     */
    public boolean isUnderlined(){
        return Boolean.TRUE.equals(underlined);
    }

    /**
     * Returns true if strikethrough is defined and set to true, false otherwise
     *
     * @return  true if strikethrough is defined and set to true, false otherwise
     */
    public boolean isStrikethrough(){
        return Boolean.TRUE.equals(strikethrough);
    }

    /**
     * Returns true if obfuscation is defined and set to true, false otherwise
     *
     * @return  true if obfuscation is defined and set to true, false otherwise
     */
    public boolean isObfuscated(){
        return Boolean.TRUE.equals(obfuscated);
    }

    @SuppressWarnings("unchecked")
    public JSONObject serialize() {
        JSONObject json = new JSONObject();
        if (color != null) json.put("color", targetVersion.getMinorVersion() >= 16 ? color.toString() : color.getLegacyColor().toString().toLowerCase());
        if (bold != null) json.put("bold", bold);
        if (italic != null) json.put("italic", italic);
        if (underlined != null) json.put("underlined", underlined);
        if (strikethrough != null) json.put("strikethrough", strikethrough);
        if (obfuscated != null) json.put("obfuscated", obfuscated);
        if (font != null) json.put("font", font);
        return json;
    }

    public String getMagicCodes() {
        StringBuilder builder = new StringBuilder();
        if (isBold()) builder.append(EnumChatFormat.BOLD.getFormat());
        if (isItalic()) builder.append(EnumChatFormat.ITALIC.getFormat());
        if (isUnderlined()) builder.append(EnumChatFormat.UNDERLINE.getFormat());
        if (isStrikethrough()) builder.append(EnumChatFormat.STRIKETHROUGH.getFormat());
        if (isObfuscated()) builder.append(EnumChatFormat.OBFUSCATED.getFormat());
        return builder.toString();
    }

    public void setTargetVersion(ProtocolVersion targetVersion) {
        this.targetVersion = targetVersion;
    }

    public ProtocolVersion getTargetVersion() {
        return targetVersion;
    }
}