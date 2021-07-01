package me.MrGraycat.eGlow.Util.Packets.MultiVersion;

public class TextColor {
	  private static EnumChatFormat[] legacyColors = EnumChatFormat.values();
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
	    this.red = Integer.valueOf(legacyColor.getRed());
	    this.green = Integer.valueOf(legacyColor.getGreen());
	    this.blue = Integer.valueOf(legacyColor.getBlue());
	    this.hexCode = legacyColor.getHexCode();
	  }
	  
	  public TextColor(int red, int green, int blue) {
	    this.red = Integer.valueOf(red);
	    this.green = Integer.valueOf(green);
	    this.blue = Integer.valueOf(blue);
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
	      this.red = Integer.valueOf(hexColor >> 16 & 0xFF);
	      this.green = Integer.valueOf(hexColor >> 8 & 0xFF);
	      this.blue = Integer.valueOf(hexColor & 0xFF);
	    } 
	    return this.red.intValue();
	  }
	  
	  public int getGreen() {
	    if (this.green == null) {
	      int hexColor = Integer.parseInt(this.hexCode.substring(1), 16);
	      this.red = Integer.valueOf(hexColor >> 16 & 0xFF);
	      this.green = Integer.valueOf(hexColor >> 8 & 0xFF);
	      this.blue = Integer.valueOf(hexColor & 0xFF);
	    } 
	    return this.green.intValue();
	  }
	  
	  public int getBlue() {
	    if (this.blue == null) {
	      int hexColor = Integer.parseInt(this.hexCode.substring(1), 16);
	      this.red = Integer.valueOf(hexColor >> 16 & 0xFF);
	      this.green = Integer.valueOf(hexColor >> 8 & 0xFF);
	      this.blue = Integer.valueOf(hexColor & 0xFF);
	    } 
	    return this.blue.intValue();
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
	  }
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
