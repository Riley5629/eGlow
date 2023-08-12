package me.mrgraycat.eglow.util.packets.chat;

import me.mrgraycat.eglow.util.packets.ProtocolVersion;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;

public class DeserializedChatComponent extends IChatBaseComponent {

	/**
	 * The original serialized component string
	 */
	private final String json;

	/**
	 * Flag tracking whether this component was fully deserialized
	 * in case of a read operation to avoid repeating the deserialization
	 * process on every request.
	 */
	private boolean deserialized;

	/**
	 * Flag tracking whether this component was modified, which means
	 * a new string has to be created instead of returning the input one.
	 */
	private boolean modified;

	/**
	 * Constructs new instance with given parameter
	 *
	 * @param json serialized component as string
	 */
	public DeserializedChatComponent(String json) {
		Preconditions.checkNotNull(json, "json");
		this.json = json;
	}

	@Override
	public String toString() {
		if (modified) return super.toString();
		return json;
	}

	@Override
	public String toString(ProtocolVersion clientVersion) {
		if (modified) return super.toString(clientVersion);
		return json;
	}

	@Override
	public List<IChatBaseComponent> getExtra() {
		if (!deserialized) deserialize();
		return super.getExtra();
	}

	@Override
	public String getText() {
		if (!deserialized) deserialize();
		return super.getText();
	}

	@Override
	public ChatModifier getModifier() {
		if (!deserialized) deserialize();
		return super.getModifier();
	}

	@Override
	public IChatBaseComponent setExtra(List<IChatBaseComponent> components) {
		if (!deserialized) deserialize();
		modified = true;
		return super.setExtra(components);
	}

	@Override
	public void addExtra(IChatBaseComponent child) {
		if (!deserialized) deserialize();
		modified = true;
		super.addExtra(child);
	}

	@Override
	public void setModifier(ChatModifier modifier) {
		if (!deserialized) deserialize();
		modified = true;
		super.setModifier(modifier);
	}

	/**
	 * Performs a full deserialization process on this component.
	 */
	@SuppressWarnings("unchecked")
	void deserialize() {
		deserialized = true;
		if (json.startsWith("\"") && json.endsWith("\"") && json.length() > 1) {
			//simple component with only text used, minecraft serializer outputs the text in quotes instead of full json
			setText(json.substring(1, json.length() - 1));
			return;
		}
		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) new JSONParser().parse(json);
		} catch (ParseException e) {
			ChatUtil.reportError(e);
			return;
		}

		setText((String) jsonObject.get("text"));
		getModifier().setBold(getBoolean(jsonObject, "bold"));
		getModifier().setItalic(getBoolean(jsonObject, "italic"));
		getModifier().setUnderlined(getBoolean(jsonObject, "underlined"));
		getModifier().setStrikethrough(getBoolean(jsonObject, "strikethrough"));
		getModifier().setObfuscated(getBoolean(jsonObject, "obfuscated"));
		getModifier().setColor(me.mrgraycat.eglow.util.packets.chat.TextColor.fromString(((String) jsonObject.get("color"))));
		if (jsonObject.containsKey("extra")) {
			List<Object> list = (List<Object>) jsonObject.get("extra");
			for (Object extra : list) {
				String string = extra.toString();
				//reverting .toString() removing "" for simple text
				if (!string.startsWith("{")) string = "\"" + string + "\"";
				addExtra(me.mrgraycat.eglow.util.packets.chat.IChatBaseComponent.deserialize(string));
			}
		}
	}

	/**
	 * Returns boolean value of requested key from map
	 *
	 * @param jsonObject map to get value from
	 * @param key        name of key
	 * @return value from json object or null if not present
	 */
	@SuppressWarnings("unchecked")
	private static Boolean getBoolean(JSONObject jsonObject, String key) {
		Preconditions.checkNotNull(jsonObject, "json object");
		Preconditions.checkNotNull(key, "key");
		String value = String.valueOf(jsonObject.getOrDefault(key, null));
		return "null".equals(value) ? null : Boolean.parseBoolean(value);
	}
}