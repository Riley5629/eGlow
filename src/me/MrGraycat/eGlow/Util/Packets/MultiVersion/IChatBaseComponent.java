package me.MrGraycat.eGlow.Util.Packets.MultiVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.MrGraycat.eGlow.Util.Text.ChatUtil;

/**
 * A class representing the n.m.s.IChatBaseComponent class to make work with it much easier
 */
@SuppressWarnings("unchecked")
public class IChatBaseComponent {
	private String text;
	private TextColor color;
	private Boolean bold;
	private Boolean italic;
	private Boolean underlined;
	private Boolean strikethrough;
	private Boolean obfuscated;
	private List<IChatBaseComponent> extra;
	  
	public IChatBaseComponent(String text) {
		this.text = text;
	}

	public List<IChatBaseComponent> getExtra() {
		if (this.extra == null)
			return Collections.EMPTY_LIST;
		return this.extra;
	}

	public IChatBaseComponent setExtra(List<IChatBaseComponent> components) {
		this.extra = components;
		return this;
	}
	  
	public IChatBaseComponent addExtra(IChatBaseComponent child) {
		if (this.extra == null)
			this.extra = new ArrayList<>();
		this.extra.add(child);
		return this;
	}

	public String getText() {
		return this.text;
	}

	public TextColor getColor() {
		return this.color;
	}

	public boolean isBold() {
		return this.bold != null && this.bold;
	}

	public boolean isItalic() {
		return this.italic != null && this.italic;
	}

	public boolean isUnderlined() {
		return this.underlined != null && this.underlined;
	}

	public boolean isStrikethrough() {
		return this.strikethrough != null && this.strikethrough;
	}

	public boolean isObfuscated() {
		return this.obfuscated != null && this.obfuscated;
	}

	public IChatBaseComponent setText(String text) {
		this.text = text;
		return this;
	}
	  
	public IChatBaseComponent setColor(TextColor color) {
		this.color = color;
		return this;
	}

	public IChatBaseComponent setBold(Boolean bold) {
		this.bold = bold;
		return this;
	}

	public IChatBaseComponent setItalic(Boolean italic) {
		this.italic = italic;
		return this;
	}

	public IChatBaseComponent setUnderlined(Boolean underlined) {
		this.underlined = underlined;
		return this;
	}

	public IChatBaseComponent setStrikethrough(Boolean strikethrough) {
		this.strikethrough = strikethrough;
		return this;
	}

	public IChatBaseComponent setObfuscated(Boolean obfuscated) {
		this.obfuscated = obfuscated;
		return this;
	}

	public static IChatBaseComponent fromString(String json) {
		try {
			if (json == null)
				return null;
			if (json.startsWith("\"") && json.endsWith("\"") && json.length() > 1)
				return new IChatBaseComponent(json.substring(1, json.length() - 1));
			JSONObject jsonObject = (JSONObject) (new JSONParser()).parse(json);
			IChatBaseComponent component = new IChatBaseComponent();
			component.setText((String) jsonObject.get("text"));
			component.setBold(getBoolean(jsonObject, "bold"));
			component.setItalic(getBoolean(jsonObject, "italic"));
			component.setUnderlined(getBoolean(jsonObject, "underlined"));
			component.setStrikethrough(getBoolean(jsonObject, "strikethrough"));
			component.setObfuscated(getBoolean(jsonObject, "obfuscated"));
			component.setColor(TextColor.fromString((String) jsonObject.get("color")));
			if (jsonObject.containsKey("extra")) {
				List<Object> list = (List<Object>) jsonObject.get("extra");
				for (Object extra : list) {
					String string = extra.toString();
					if (!string.startsWith("{"))
						string = "\"" + string + "\"";
					component.addExtra(fromString(string));
				}
			}
			return component;
		} catch (Exception e) {
			// TAB.getInstance().debug("Failed to parse json object: " + json);
			return fromColoredText(json);
		}// TAB.getInstance().getErrorManager().printError("Failed to read component: " +
// json, e);

	}

	private static Boolean getBoolean(JSONObject jsonObject, String key) {
		if (jsonObject.containsKey(key))
			return Boolean.parseBoolean(String.valueOf(jsonObject.get(key)));
		return null;
	}

	public String toString() {
		JSONObject json = new JSONObject();
		if (this.text != null)
			json.put("text", this.text);
		if (this.color != null)
			json.put("color", this.color.toString());
		if (this.bold != null)
			json.put("bold", this.bold);
		if (this.italic != null)
			json.put("italic", this.italic);
		if (this.underlined != null)
			json.put("underlined", this.underlined);
		if (this.strikethrough != null)
			json.put("strikethrough", this.strikethrough);
		if (this.obfuscated != null)
			json.put("obfuscated", this.obfuscated);
		if (this.extra != null)
			json.put("extra", this.extra);
		return json.toString();
	}

	public String toString(ProtocolVersion clientVersion) {
		return toString(clientVersion, false);
	}

	public String toString(ProtocolVersion clientVersion, boolean sendTranslatableIfEmpty) {
		if (this.extra == null) {
			if (this.text == null)
				return null;
			if (this.text.length() == 0) {
				if (sendTranslatableIfEmpty)
					return "{\"translate\":\"\"}";
				return "{\"text\":\"\"}";
			}
		}
		if (clientVersion.getMinorVersion() < 16)
			convertColorsToLegacy();
		return toString();
	}

	private void convertColorsToLegacy() {
		if (this.color != null)
			this.color.setReturnLegacy(true);
		for (IChatBaseComponent extra : getExtra())
			extra.convertColorsToLegacy();
	}

	public static IChatBaseComponent fromColoredText(String originalText) {
		String text = ChatUtil.translateColors(originalText);
		List<IChatBaseComponent> components = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		IChatBaseComponent component = new IChatBaseComponent();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '§') {
				i++;
				if (i >= text.length())
					break;
				c = text.charAt(i);
				if (c >= 'A' && c <= 'Z')
					c = (char) (c + 32);
				EnumChatFormat format = EnumChatFormat.getByChar(c);
				if (format != null) {
					if (builder.length() > 0) {
						component.setText(builder.toString());
						components.add(component);
						component = component.copyFormatting();
						builder = new StringBuilder();
					}
					switch (format) {
					case BOLD:
						component.setBold(Boolean.TRUE);
						break;
					case ITALIC:
						component.setItalic(Boolean.TRUE);
						break;
					case UNDERLINE:
						component.setUnderlined(Boolean.TRUE);
						break;
					case STRIKETHROUGH:
						component.setStrikethrough(Boolean.TRUE);
						break;
					case OBFUSCATED:
						component.setObfuscated(Boolean.TRUE);
						break;
					case RESET:
						component = new IChatBaseComponent();
						component.setColor(new TextColor(EnumChatFormat.WHITE));
						break;
					default:
						component = new IChatBaseComponent();
						component.setColor(new TextColor(format));
						break;
					}
				}
			} else if (c == '#') {
				try {
					TextColor color;
					String hex = text.substring(i, i + 7);
					Integer.parseInt(hex.substring(1), 16);
					if (containsLegacyCode(text, i)) {
						color = new TextColor(hex, EnumChatFormat.getByChar(text.charAt(i + 8)));
						i += 8;
					} else {
						color = new TextColor(hex);
						i += 6;
					}
					if (builder.length() > 0) {
						component.setText(builder.toString());
						components.add(component);
						builder = new StringBuilder();
					}
					component = new IChatBaseComponent();
					component.setColor(color);
				} catch (Exception e) {
					builder.append(c);
				}
			} else {
				builder.append(c);
			}
		}
		component.setText(builder.toString());
		components.add(component);
		return (new IChatBaseComponent("")).setExtra(components);
	}

	private static boolean containsLegacyCode(String text, int i) {
		if (text.length() - i < 9 || text.charAt(i + 7) != '|')
			return false;
		return (EnumChatFormat.getByChar(text.charAt(i + 8)) != null);
	}

	public IChatBaseComponent copyFormatting() {
		IChatBaseComponent component = new IChatBaseComponent();
		component.setBold(this.bold);
		component.setColor(this.color);
		component.setItalic(this.italic);
		component.setObfuscated(this.obfuscated);
		component.setStrikethrough(this.strikethrough);
		component.setUnderlined(this.underlined);
		return component;
	}

	public String toLegacyText() {
		StringBuilder builder = new StringBuilder();
		append(builder, "");
		return builder.toString();
	}

	private String append(StringBuilder builder, String previousFormatting) {
		String formatting = previousFormatting;
		if (this.text != null) {
			formatting = getFormatting();
			if (!formatting.equals(previousFormatting))
				builder.append(formatting);
			builder.append(this.text);
		}
		for (IChatBaseComponent component : getExtra())
			formatting = component.append(builder, formatting);
		return formatting;
	}

	private String getFormatting() {
		StringBuilder builder = new StringBuilder();
		if (this.color != null)
			if (this.color.getLegacyColor() == EnumChatFormat.WHITE) {
				builder.append(EnumChatFormat.RESET.getFormat());
			} else {
				builder.append(this.color.getLegacyColor().getFormat());
			}
		if (isBold())
			builder.append(EnumChatFormat.BOLD.getFormat());
		if (isItalic())
			builder.append(EnumChatFormat.ITALIC.getFormat());
		if (isUnderlined())
			builder.append(EnumChatFormat.UNDERLINE.getFormat());
		if (isStrikethrough())
			builder.append(EnumChatFormat.STRIKETHROUGH.getFormat());
		if (isObfuscated())
			builder.append(EnumChatFormat.OBFUSCATED.getFormat());
		return builder.toString();
	}

	public String toRawText() {
		StringBuilder builder = new StringBuilder();
		if (this.text != null)
			builder.append(this.text);
		for (IChatBaseComponent extra : getExtra()) {
			if (extra.text != null)
				builder.append(extra.text);
		}
		return builder.toString();
	}

	public String toFlatText() {
		StringBuilder builder = new StringBuilder();
		if (this.color != null)
			builder.append(this.color.getHexCode());
		if (isBold())
			builder.append(EnumChatFormat.BOLD.getFormat());
		if (isItalic())
			builder.append(EnumChatFormat.ITALIC.getFormat());
		if (isUnderlined())
			builder.append(EnumChatFormat.UNDERLINE.getFormat());
		if (isStrikethrough())
			builder.append(EnumChatFormat.STRIKETHROUGH.getFormat());
		if (isObfuscated())
			builder.append(EnumChatFormat.OBFUSCATED.getFormat());
		if (this.text != null)
			builder.append(this.text);
		for (IChatBaseComponent extra : getExtra())
			builder.append(extra.toFlatText());
		return builder.toString();
	}

	public IChatBaseComponent clone() {
		IChatBaseComponent component = new IChatBaseComponent(this.text);
		component.setBold(this.bold);
		component.setColor(this.color);
		component.setItalic(this.italic);
		component.setObfuscated(this.obfuscated);
		component.setStrikethrough(this.strikethrough);
		component.setUnderlined(this.underlined);
		for (IChatBaseComponent extra : getExtra())
			component.addExtra(extra.clone());
		return component;
	}

	public static IChatBaseComponent optimizedComponent(String text) {
		if (text == null)
			return null;
		if (text.contains("#") || text.contains("&x") || text.contains("§x"))
			return fromColoredText(text);
		return new IChatBaseComponent(text);
	}

	public IChatBaseComponent() {
	}

	/*public enum ClickAction {
		OPEN_URL, RUN_COMMAND, CHANGE_PAGE, SUGGEST_COMMAND, COPY_TO_CLIPBOARD;
	}

	/*public enum HoverAction {
		SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY;

		public static HoverAction fromString(String s) {
			for (HoverAction action : values()) {
				if (s.toUpperCase().contains(action.toString()))
					return action;
			}
			throw new IllegalArgumentException("HoverAction not found by name " + s);
		}
	}
	/*public static final String EMPTY_TRANSLATABLE = "{\"translate\":\"\"}";
	public static final String EMPTY_TEXT = "{\"text\":\"\"}";

	private String text;
	private TextColor color;

	private Boolean bold;
	private Boolean italic;
	private Boolean underlined;
	private Boolean strikethrough;
	private Boolean obfuscated;

	private ClickAction clickAction;
	private Object clickValue;
	private HoverAction hoverAction;
	private String hoverValue;

	private List<IChatBaseComponent> extra;
	private JSONObject jsonObject = new JSONObject();

	public IChatBaseComponent() {
	}
	public IChatBaseComponent(String text) {
		setText(text);
	}

	public List<IChatBaseComponent> getExtra(){
		return extra;
	}
	public IChatBaseComponent setExtra(List<IChatBaseComponent> components){
		this.extra = components;
		jsonObject.put("extra", extra);
		return this;
	}
	public IChatBaseComponent addExtra(IChatBaseComponent child) {
		if (extra == null) {
			extra = new ArrayList<IChatBaseComponent>();
			jsonObject.put("extra", extra);
		}
		extra.add(child);
		return this;
	}

	public String getText() {
		return text;
	}
	public TextColor getColor() {
		return color;
	}
	public boolean isBold(){
		return bold == null ? false : bold;
	}
	public boolean isItalic(){
		return italic == null ? false : italic;
	}
	public boolean isUnderlined(){
		return underlined == null ? false : underlined;
	}
	public boolean isStrikethrough(){
		return strikethrough == null ? false : strikethrough;
	}
	public boolean isObfuscated(){
		return obfuscated == null ? false : obfuscated;
	}

	public IChatBaseComponent setText(String text) {
		this.text = text;
		if (text != null) {
			jsonObject.put("text", text);
		} else {
			jsonObject.remove("text");
		}
		return this;
	}
	public IChatBaseComponent setColor(TextColor color) {
		this.color = color;
		return this;
	}
	public IChatBaseComponent setBold(Boolean bold) {
		this.bold = bold;
		if (bold != null) {
			jsonObject.put("bold", bold);
		} else {
			jsonObject.remove("bold");
		}
		return this;
	}
	public IChatBaseComponent setItalic(Boolean italic) {
		this.italic = italic;
		if (italic != null) {
			jsonObject.put("italic", italic);
		} else {
			jsonObject.remove("italic");
		}
		return this;
	}
	public IChatBaseComponent setUnderlined(Boolean underlined) {
		this.underlined = underlined;
		if (underlined != null) {
			jsonObject.put("underlined", underlined);
		} else {
			jsonObject.remove("underlined");
		}
		return this;
	}
	public IChatBaseComponent setStrikethrough(Boolean strikethrough) {
		this.strikethrough = strikethrough;
		if (strikethrough != null) {
			jsonObject.put("strikethrough", strikethrough);
		} else {
			jsonObject.remove("strikethrough");
		}
		return this;
	}
	public IChatBaseComponent setObfuscated(Boolean obfuscated) {
		this.obfuscated = obfuscated;
		if (obfuscated != null) {
			jsonObject.put("obfuscated", obfuscated);
		} else {
			jsonObject.remove("obfuscated");
		}
		return this;
	}

	public ClickAction getClickAction() {
		return clickAction;
	}
	public Object getClickValue() {
		return clickValue;
	}

	public IChatBaseComponent onClickOpenUrl(String url) {
		return onClick(ClickAction.OPEN_URL, url);
	}
	public IChatBaseComponent onClickRunCommand(String command) {
		return onClick(ClickAction.RUN_COMMAND, command);
	}
	public IChatBaseComponent onClickSuggestCommand(String command) {
		return onClick(ClickAction.SUGGEST_COMMAND, command);
	}
	public IChatBaseComponent onClickChangePage(int newpage) {
		return onClick(ClickAction.CHANGE_PAGE, newpage);
	}
	private IChatBaseComponent onClick(ClickAction action, Object value) {
		clickAction = action;
		clickValue = value;
		JSONObject click = new JSONObject();
		click.put("action", action.toString().toLowerCase());
		click.put("value", value);
		jsonObject.put("clickEvent", click);
		return this;
	}

	public HoverAction getHoverAction() {
		return hoverAction;
	}
	public String getHoverValue() {
		return hoverValue;
	}

	public IChatBaseComponent onHoverShowText(String text) {
		return onHover(HoverAction.SHOW_TEXT, text);
	}

	public IChatBaseComponent onHoverShowItem(String serializedItem) {
		return onHover(HoverAction.SHOW_ITEM, serializedItem);
	}

	public IChatBaseComponent onHoverShowEntity(UUID id, String customname, String type) {
		JSONObject json = new JSONObject();
		json.put("id", id.toString());
		if (type != null) json.put("type", type);
		if (customname != null) json.put("name", customname);
		return onHover(HoverAction.SHOW_ENTITY, json.toString());
	}
	private IChatBaseComponent onHover(HoverAction action, String value) {
		hoverAction = action;
		hoverValue = value;
		JSONObject hover = new JSONObject();
		hover.put("action", action.toString().toLowerCase());
		hover.put("value", value);
		jsonObject.put("hoverEvent", hover);
		return this;
	}



	public static IChatBaseComponent fromString(String json) {
		try {
			if (json == null) return null;
			if (json.startsWith("\"") && json.endsWith("\"")) {
				//simple component with only text used, minecraft serializer outputs the text in quotes instead of full json
				return new IChatBaseComponent(json.substring(1, json.length()-1));
			}
			JSONObject jsonObject = ((JSONObject) new JSONParser().parse(json));
			IChatBaseComponent component = new IChatBaseComponent();
			component.setText((String) jsonObject.get("text"));
			component.setBold(getBoolean(jsonObject, "bold"));
			component.setItalic(getBoolean(jsonObject, "italic"));
			component.setUnderlined(getBoolean(jsonObject, "underlined"));
			component.setStrikethrough(getBoolean(jsonObject, "strikethrough"));
			component.setObfuscated(getBoolean(jsonObject, "obfuscated"));
			component.setColor(TextColor.fromString(((String) jsonObject.get("color"))));
			if (jsonObject.containsKey("clickEvent")) {
				JSONObject clickEvent = (JSONObject) jsonObject.get("clickEvent");
				String action = (String) clickEvent.get("action");
				Object value = (Object) clickEvent.get("value");
				component.onClick(ClickAction.valueOf(action.toUpperCase()), value);
			}
			if (jsonObject.containsKey("hoverEvent")) {
				JSONObject hoverEvent = (JSONObject) jsonObject.get("hoverEvent");
				String action = (String) hoverEvent.get("action");
				String value = (String) hoverEvent.get("value");
				component.onHover(HoverAction.valueOf(action.toUpperCase()), value);
			}
			if (jsonObject.containsKey("extra")) {
				List<Object> list = (List<Object>) jsonObject.get("extra");
				for (Object extra : list) {
					component.addExtra(fromString(extra.toString()));
				}
			}
			return component;
		} catch (ParseException e) {
			return fromColoredText(json);
		} catch (Exception e) {
			return fromColoredText(json);
		}
	}
	
	private static Boolean getBoolean(JSONObject jsonObject, String key) {
		if (jsonObject.containsKey(key)) {
			return Boolean.parseBoolean(String.valueOf(jsonObject.get(key)));
		}
		return null;
	}
	
	public String toString(ProtocolVersion clientVersion) {
		return toString(clientVersion, false);
	}
	public String toString(ProtocolVersion clientVersion, boolean sendTranslatableIfEmpty) {
		if (extra == null) {
			if (text == null) return null;
			if (text.length() == 0) {
				if (sendTranslatableIfEmpty) {
					return EMPTY_TRANSLATABLE;
				} else {
					return EMPTY_TEXT;
				}
			}
		}
		//the core component, fixing all colors
		if (color != null) {
			jsonObject.put("color", color.toString(clientVersion.getMinorVersion() >= 16));
		}
		if (extra != null) {
			for (IChatBaseComponent extra : extra) {
				if (extra.color != null) {
					extra.jsonObject.put("color", extra.color.toString(clientVersion.getMinorVersion() >= 16));
				}
			}
		}
		return toString();
	}

	@Override
	public String toString() {
		return jsonObject.toString();
	}

	public static IChatBaseComponent fromColoredText(String originalText){
		if (originalText == null) return new IChatBaseComponent();
		String text = originalText;
		//text = RGBUtils.applyFormats(text);
		List<IChatBaseComponent> components = new ArrayList<IChatBaseComponent>();
		StringBuilder builder = new StringBuilder();
		IChatBaseComponent component = new IChatBaseComponent();
		for (int i = 0; i < text.length(); i++){
			char c = text.charAt(i);
			if (c == EnumChatFormat.colorChar || c == '&'){
				i++;
				if (i >= text.length()) {
					break;
				}
				c = text.charAt(i);
				if ((c >= 'A') && (c <= 'Z')) {
					c = (char)(c + ' ');
				}
				EnumChatFormat format = EnumChatFormat.getByChar(c);
				if (format != null){
					if (builder.length() > 0) {
						TextColor color = component.getColor();
						component.setText(builder.toString());
						components.add(component);
						component = new IChatBaseComponent();
						builder = new StringBuilder();
						component.setColor(color);
					}
					switch (format){
					case BOLD: 
						component.setBold(true);
						break;
					case ITALIC: 
						component.setItalic(true);
						break;
					case UNDERLINE: 
						component.setUnderlined(true);
						break;
					case STRIKETHROUGH: 
						component.setStrikethrough(true);
						break;
					case OBFUSCATED: 
						component.setObfuscated(true);
						break;
					case RESET: 
						component.setColor(TextColor.of(EnumChatFormat.WHITE));
						break;
					default:
						component.setColor(TextColor.of(format));
						break;
					}
				}
			} else if (c == '#'){
				try {
					String hex = text.substring(i+1, i+7);
					TextColor color = TextColor.of(hex); //the validation check is in constructor

					if (builder.length() > 0){
						component.setText(builder.toString());
						components.add(component);
						builder = new StringBuilder();
					}
					component = new IChatBaseComponent();
					component.setColor(color);
					i += 6;
				} catch (Exception e) {
					//invalid hex code
					builder.append(c);
				}
			} else {
				builder.append(c);
			}
		}
		component.setText(builder.toString());
		components.add(component);
		return new IChatBaseComponent("").setExtra(components);
	}
	
	**
	 * Converts this component into a simple text with legacy colors (closest match if color is set to RGB)
	 * @return The simple text format
	
	public String toLegacyText() {
		StringBuilder builder = new StringBuilder();
		if (color != null) {
			if (color.getLegacyColor() == EnumChatFormat.WHITE) {
				//preventing unwanted &r -> &f conversion and stopping the <1.13 client bug fix from working
				builder.append(EnumChatFormat.RESET.getFormat());
			} else {
				builder.append(color.getLegacyColor().getFormat());
			}
		}
		if (isBold()) builder.append(EnumChatFormat.BOLD.getFormat());
		if (isItalic()) builder.append(EnumChatFormat.ITALIC.getFormat());
		if (isUnderlined()) builder.append(EnumChatFormat.UNDERLINE.getFormat());
		if (isStrikethrough()) builder.append(EnumChatFormat.STRIKETHROUGH.getFormat());
		if (isObfuscated()) builder.append(EnumChatFormat.OBFUSCATED.getFormat());
		if (text != null) builder.append(text);
		if (extra != null) {
			for (IChatBaseComponent component : extra) {
				builder.append(component.toLegacyText());
			}
		}
		return builder.toString();
	}

	public String toRawText() {
		StringBuilder builder = new StringBuilder();
		if (text != null) builder.append(text);
		if (extra != null) {
			for (IChatBaseComponent extra : extra) {
				if (extra.text != null) builder.append(extra.text);
			}
		}
		return builder.toString();
	}
	
	public static IChatBaseComponent optimizedComponent(String text){
		return text != null && (text.contains("#") || text.contains("&x") || text.contains(EnumChatFormat.colorChar + "x")) ? IChatBaseComponent.fromColoredText(text) : new IChatBaseComponent(text);
	}
	public enum ClickAction{
		OPEN_URL,
		@Deprecated OPEN_FILE,//Cannot be sent by server
		RUN_COMMAND,
		@Deprecated TWITCH_USER_INFO, //Removed in 1.9
		CHANGE_PAGE,
		SUGGEST_COMMAND,
		COPY_TO_CLIPBOARD; //since 1.15
	}
	public enum HoverAction{
		SHOW_TEXT,
		SHOW_ITEM,
		SHOW_ENTITY,
		@Deprecated SHOW_ACHIEVEMENT;//Removed in 1.12
	}*/
}
