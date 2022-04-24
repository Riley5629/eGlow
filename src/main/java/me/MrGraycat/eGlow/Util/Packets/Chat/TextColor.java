package me.MrGraycat.eGlow.Util.Packets.Chat;

public class TextColor {
	/**
	 * RGB values as a single number of 3 8-bit numbers (0-255).
	 * It is only initialized if colors are actually used to avoid
	 * unnecessary memory allocations with string operations.
	 */
	private int rgb = -1;

	/** Closest legacy color to this color object. */
	private EnumChatFormat legacyColor;

	/** HEX code of this color as a 6-digit hexadecimal string without "#" prefix. */
	private String hexCode;

	/**
	 * Boolean value whether the legacy color was forced with constructor or should be
	 * automatically assigned as closest color.
	 * This value is used in gradients when converting text for legacy players. */
	private boolean legacyColorForced;

	/**
	 * Constructs new instance as a clone of the provided color.
	 *
	 * @param   color
	 *          color to create a clone of
	 * @throws  IllegalArgumentException
	 *          if color is {@code null}
	 */
	public TextColor(TextColor color) {
		Preconditions.checkNotNull(color, "color");
		rgb = color.rgb;
		legacyColor = color.legacyColor;
		hexCode = color.hexCode;
		legacyColorForced = color.legacyColorForced;
	}

	/**
	 * Constructs new instance from provided 6-digit hex code string
	 *
	 * @param   hexCode
	 *          a 6-digit combination of hex numbers as a string
	 * @throws  IllegalArgumentException
	 *          if hexCode is {@code null}
	 */
	public TextColor(String hexCode) {
		Preconditions.checkNotNull(hexCode, "hex code");
		this.hexCode = hexCode;
	}

	/**
	 * Constructs new instance from provided 6-digit hex code and forced legacy color
	 *
	 * @param   hexCode
	 *          6-digit combination of hex numbers as a string
	 * @param   legacyColor
	 *          color to use for legacy clients instead of using the closest legacy color
	 * @throws  IllegalArgumentException
	 *          if {@code hexCode} is {@code null} or {@code legacyColor} is {@code null}
	 */
	public TextColor(String hexCode, EnumChatFormat legacyColor) {
		Preconditions.checkNotNull(hexCode, "hex code");
		Preconditions.checkNotNull(legacyColor, "legacy color");
		this.hexCode = hexCode;
		this.legacyColorForced = true;
		this.legacyColor = legacyColor;
	}

	/**
	 * Constructs new instance from provided legacy color
	 *
	 * @param   legacyColor
	 *          legacy color to construct the instance from
	 * @throws  IllegalArgumentException
	 *          if {@code legacyColor} is {@code null}
	 */
	public TextColor(EnumChatFormat legacyColor) {
		Preconditions.checkNotNull(legacyColor, "legacy color");
		this.rgb = (legacyColor.getRed() << 16) + (legacyColor.getGreen() << 8) + legacyColor.getBlue();
		this.hexCode = legacyColor.getHexCode();
	}

	/**
	 * Constructs new instance with red, green and blue values
	 *
	 * @param   red
	 *          red value
	 * @param   green
	 *          green value
	 * @param   blue
	 *          blue value
	 * @throws  IllegalArgumentException
	 *          if {@code red}, {@code green} or {@code blue} is out of range ({@code 0-255})
	 */
	public TextColor(int red, int green, int blue) {
		Preconditions.checkRange(red, 0, 255, "red");
		Preconditions.checkRange(green, 0, 255, "green");
		Preconditions.checkRange(blue, 0, 255, "blue");
		this.rgb = (red << 16) + (green << 8) + blue;
	}

	/**
	 * Loads the closest legacy color based currently provided values
	 */
	private EnumChatFormat loadClosestColor() {
		double minMaxDist = 9999;
		double maxDist;
		EnumChatFormat closestColor = EnumChatFormat.WHITE;
		for (EnumChatFormat color : EnumChatFormat.VALUES) {
			int rDiff = Math.abs(color.getRed() - getRed());
			int gDiff = Math.abs(color.getGreen() - getGreen());
			int bDiff = Math.abs(color.getBlue() - getBlue());
			maxDist = rDiff;
			if (gDiff > maxDist) maxDist = gDiff;
			if (bDiff > maxDist) maxDist = bDiff;
			if (maxDist < minMaxDist) {
				minMaxDist = maxDist;
				closestColor = color;
			}
		}
		return closestColor;
	}

	/**
	 * Returns {@code red} value
	 *
	 * @return  red value
	 */
	public int getRed() {
		if (rgb == -1) rgb = Integer.parseInt(hexCode, 16);
		return (rgb >> 16) & 0xFF;
	}

	/**
	 * Returns {@code green} value
	 *
	 * @return  green value
	 */
	public int getGreen() {
		if (rgb == -1) rgb = Integer.parseInt(hexCode, 16);
		return (rgb >> 8) & 0xFF;
	}

	/**
	 * Returns {@code blue} value
	 *
	 * @return  blue value
	 */
	public int getBlue() {
		if (rgb == -1) rgb = Integer.parseInt(hexCode, 16);
		return rgb & 0xFF;
	}

	/**
	 * Returns the closest legacy color of this color object.
	 * If the color was defined in constructor, it's returned.
	 * Otherwise, the closest color is calculated the then returned.
	 *
	 * @return  closest legacy color
	 */
	public EnumChatFormat getLegacyColor() {
		if (legacyColor == null) legacyColor = loadClosestColor();
		return legacyColor;
	}

	/**
	 * Returns the rgb combination as a 6-digit hex code string
	 *
	 * @return  the rgb combination as a 6-digit hex code string
	 */
	public String getHexCode() {
		if (hexCode == null) hexCode = String.format("%06X", rgb);
		return hexCode;
	}

	/**
	 * Converts the color into a valid color value used in color field in chat component.
	 * That is either 6-digit hex code prefixed with '#', or lowercase legacy color.
	 *
	 * @return  the color serialized for use in chat component
	 */
	public String toString() {
		EnumChatFormat legacyEquivalent = EnumChatFormat.fromRGBExact(getRed(), getGreen(), getBlue());
		if (legacyEquivalent != null) {
			//not sending old colors as RGB to 1.16 clients if not needed as <1.16 servers will fail to apply color
			return legacyEquivalent.toString().toLowerCase();
		}
		return "#" + getHexCode();
	}

	/**
	 * Returns true if legacy color was forced with a constructor, false if not
	 *
	 * @return  true if forced, false if not
	 */
	public boolean isLegacyColorForced() {
		return legacyColorForced;
	}

	/**
	 * Reads the string as it appears in chat component and turns it into the color object.
	 * If the entered string is null, returns null.
	 * If it's prefixed with '#', it's considered as a hex code.
	 * Otherwise, it is considered being a lowercase legacy color.
	 *
	 * @param   string
	 *          string from color field in chat component
	 * @return  An instance from specified string or null if string is null
	 */
	public static TextColor fromString(String string) {
		if (string == null) return null;
		if (string.startsWith("#")) return new TextColor(string.substring(1));
		return new TextColor(EnumChatFormat.valueOf(string.toUpperCase()));
	}
	  /*private static final EnumChatFormat[] legacyColors = EnumChatFormat.values();
	  private Integer red;
	  private Integer green;
	  private Integer blue;
	  private EnumChatFormat legacyColor;
	  private String hexCode;
	  private boolean legacyColorForced;
	  private boolean returnLegacy;
	  
	  public TextColor(String hexCode) {
	    this.hexCode = hexCode;
	  }
	  
	  public TextColor(String hexCode, EnumChatFormat legacyColor) {
	    this.hexCode = hexCode;
	    this.legacyColorForced = true;
	    this.legacyColor = legacyColor;
	  }
	  
	  public TextColor(EnumChatFormat legacyColor) {
	    this.red = legacyColor.getRed();
	    this.green = legacyColor.getGreen();
	    this.blue = legacyColor.getBlue();
	    this.hexCode = legacyColor.getHexCode();
	  }
	  
	  public TextColor(int red, int green, int blue) {
	    this.red = red;
	    this.green = green;
	    this.blue = blue;
	  }
	  
	  private EnumChatFormat getClosestColor(int red, int green, int blue) {
	    double minMaxDist = 9999.0D;
	    EnumChatFormat legacyColor = EnumChatFormat.WHITE;
	    for (EnumChatFormat color : legacyColors) {
	      int rDiff = color.getRed() - red;
	      int gDiff = color.getGreen() - green;
	      int bDiff = color.getBlue() - blue;
	      if (rDiff < 0)
	        rDiff = -rDiff; 
	      if (gDiff < 0)
	        gDiff = -gDiff; 
	      if (bDiff < 0)
	        bDiff = -bDiff; 
	      double maxDist = rDiff;
	      if (gDiff > maxDist)
	        maxDist = gDiff; 
	      if (bDiff > maxDist)
	        maxDist = bDiff; 
	      if (maxDist < minMaxDist) {
	        minMaxDist = maxDist;
	        legacyColor = color;
	      } 
	    } 
	    return legacyColor;
	  }
	  
	  public int getRed() {
	    if (this.red == null) {
	      int hexColor = Integer.parseInt(this.hexCode.substring(1), 16);
	      this.red = hexColor >> 16 & 0xFF;
	      this.green = hexColor >> 8 & 0xFF;
	      this.blue = hexColor & 0xFF;
	    } 
	    return this.red;
	  }
	  
	  public int getGreen() {
	    if (this.green == null) {
	      int hexColor = Integer.parseInt(this.hexCode.substring(1), 16);
	      this.red = hexColor >> 16 & 0xFF;
	      this.green = hexColor >> 8 & 0xFF;
	      this.blue = hexColor & 0xFF;
	    } 
	    return this.green;
	  }
	  
	  public int getBlue() {
	    if (this.blue == null) {
	      int hexColor = Integer.parseInt(this.hexCode.substring(1), 16);
	      this.red = hexColor >> 16 & 0xFF;
	      this.green = hexColor >> 8 & 0xFF;
	      this.blue = hexColor & 0xFF;
	    } 
	    return this.blue;
	  }
	  
	  public EnumChatFormat getLegacyColor() {
	    if (this.legacyColor == null)
	      this.legacyColor = getClosestColor(getRed(), getGreen(), getBlue()); 
	    return this.legacyColor;
	  }
	  
	  public String getHexCode() {
	    if (this.hexCode == null)
	      this.hexCode = String.format("#%06X", new Object[] { Integer.valueOf((this.red.intValue() << 16) + (this.green.intValue() << 8) + this.blue.intValue()) }); 
	    return this.hexCode;
	  }
	  
	  public String toString() {
	    if (!this.returnLegacy) {
	      EnumChatFormat legacyEquivalent = EnumChatFormat.fromRGBExact(getRed(), getGreen(), getBlue());
	      if (legacyEquivalent != null)
	        return legacyEquivalent.toString().toLowerCase(); 
	      return getHexCode();
	    } 
	    return getLegacyColor().toString().toLowerCase();
	  }
	  
	  public boolean isLegacyColorForced() {
	    return this.legacyColorForced;
	  }
	  
	  public void setReturnLegacy(boolean returnLegacy) {
	    this.returnLegacy = returnLegacy;
	  }
	  
	  public static TextColor fromString(String string) {
	    if (string == null)
	      return null; 
	    if (string.startsWith("#"))
	      return new TextColor(string); 
	    return new TextColor(EnumChatFormat.valueOf(string.toUpperCase()));
	  }*/
	/*
		//red value
		private int red;
		
		//green value
		private int green;
		
		//blue value
		private int blue;
		
		//closest legacy color
		private EnumChatFormat legacyColor;

		/**
		 * Constructs new instance with all argments
		 * Private, use TextColor.of methods
		 * @param red - red value
		 * @param green - green value
		 * @param blue - blue value
		 * @param legacyColor - closest legacy color
		 *
		private TextColor(int red, int green, int blue, EnumChatFormat legacyColor) {
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.legacyColor = legacyColor;
		}

		/**
		 * Returns amount of red
		 * @return amount of red
		 *
		public int getRed() {
			return red;
		}
		
		/**
		 * Returns amount of green
		 * @return amount of green
		 *
		public int getGreen() {
			return green;
		}
		
		/**
		 * Returns amount of blue
		 * @return amount of blue
		 *
		public int getBlue() {
			return blue;
		}
		
		/**
		 * Returns the closest legacy color
		 * @return closest legacy color
		 *
		public EnumChatFormat getLegacyColor() {
			return legacyColor;
		}
		
		/**
		 * Converts the color into a valid color value used in color field in chat component
		 * @param rgbClient - if client accepts RGB or not
		 * @return the color converted into string acceptable by client
		 *
		public String toString(boolean rgbClient) {
			return legacyColor.toString().toLowerCase();
			/*if (rgbClient) {
				EnumChatFormat legacyEquivalent = EnumChatFormat.fromRGBExact(red, green, blue);
				if (legacyEquivalent != null) {
					//not sending old colors as RGB to 1.16 clients if not needed as <1.16 servers will fail to apply color
					return legacyEquivalent.toString().toLowerCase();
				}
				return "#" + RGBUtils.toHexString(red, green, blue);
			} else {
				return legacyColor.toString().toLowerCase();
			}*
		}
		
		/**
		 * Reads the string and turns into text color. String is either #RRGGBB or a lowercased legacy color
		 * @param string - string from color field in chat component
		 * return An instance from specified string
		 *
		public static TextColor fromString(String string) {
			if (string == null) return null;
			if (string.startsWith("#")) {
				return of(string.substring(1));
			} else {
				return of(EnumChatFormat.valueOf(string.toUpperCase()));
			}
		}
		
		/**
		 * Returns a new instance based on hex code as string
		 * @param hexCode - a 6-digit combination of hex numbers
		 * @return TextColor from hex color
		 *
		public static TextColor of(String hexCode) {
			int hexColor = Integer.parseInt(hexCode, 16);
			int red = ((hexColor >> 16) & 0xFF);
			int green = ((hexColor >> 8) & 0xFF);
			int blue = (hexColor & 0xFF);
			return of(red, green, blue);
		}
		
		/**
		 * Returns a new instance based on legacy color
		 * @param legacyColor - legacy color
		 * @return TextColor from legacy color
		 *
		public static TextColor of(EnumChatFormat legacyColor) {
			int red = legacyColor.getRed();
			int green = legacyColor.getGreen();
			int blue = legacyColor.getBlue();
			return new TextColor(red, green, blue, legacyColor);
		}
		
		/**
		 * Returns a new instance based on color bytes
		 * @param red - red value
		 * @param green - green value
		 * @param blue - blue value
		 * @return TextColor from RGB combination
		 *
		public static TextColor of(int red, int green, int blue) {
			double minDist = 9999;
			double dist;
			EnumChatFormat legacyColor = EnumChatFormat.WHITE;
			for (EnumChatFormat color : EnumChatFormat.values()) {
				int rDiff = (int) Math.pow(color.getRed() - red, 2);
				int gDiff = (int) Math.pow(color.getGreen() - green, 2);
				int bDiff = (int) Math.pow(color.getBlue() - blue, 2);
				dist = Math.sqrt(rDiff + gDiff + bDiff);
				if (dist < minDist) {
					minDist = dist;
					legacyColor = color;
				}
			}
			return new TextColor(red, green, blue, legacyColor);
		}
		
	*/
}
