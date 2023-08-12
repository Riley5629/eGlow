package me.mrgraycat.eglow.util.packets.chat;

import me.mrgraycat.eglow.util.packets.ProtocolVersion;
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

	@SuppressWarnings("unchecked")
	public JSONObject serialize() {
		JSONObject json = new JSONObject();
		if (color != null)
			json.put("color", targetVersion.getMinorVersion() >= 16 ? color.toString() : color.getLegacyColor().toString().toLowerCase());
		if (bold != null) json.put("bold", bold);
		if (italic != null) json.put("italic", italic);
		if (underlined != null) json.put("underlined", underlined);
		if (strikethrough != null) json.put("strikethrough", strikethrough);
		if (obfuscated != null) json.put("obfuscated", obfuscated);
		if (font != null) json.put("font", font);
		return json;
	}

	public void setTargetVersion(ProtocolVersion targetVersion) {
		this.targetVersion = targetVersion;
	}

	public ProtocolVersion getTargetVersion() {
		return targetVersion;
	}
}