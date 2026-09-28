package dev.eden.client;

public final class MoulConfigInputCompat {
	private static final int SYNTHETIC_KEY_BASE = 10_000;
	private static final int MAX_SDL_SCANCODE = 512;

	private MoulConfigInputCompat() {
	}

	public static int toGlfwMouseButton(int sdlButton) {
		return switch (sdlButton) {
			case 1 -> 0;
			case 2 -> 2;
			case 3 -> 1;
			default -> sdlButton - 1;
		};
	}

	public static int toSdlMouseButton(int glfwButton) {
		return switch (glfwButton) {
			case 0 -> 1;
			case 1 -> 3;
			case 2 -> 2;
			default -> glfwButton + 1;
		};
	}

	public static int toGlfwKey(int sdlScancode) {
		if (sdlScancode >= 4 && sdlScancode <= 29) return 65 + (sdlScancode - 4);
		if (sdlScancode >= 30 && sdlScancode <= 38) return 49 + (sdlScancode - 30);
		if (sdlScancode == 39) return 48;
		if (sdlScancode >= 58 && sdlScancode <= 69) return 290 + (sdlScancode - 58);
		if (sdlScancode >= 104 && sdlScancode <= 115) return 302 + (sdlScancode - 104);
		if (sdlScancode >= 89 && sdlScancode <= 97) return 321 + (sdlScancode - 89);

		return switch (sdlScancode) {
			case 40 -> 257;
			case 41 -> 256;
			case 42 -> 259;
			case 43 -> 258;
			case 44 -> 32;
			case 45 -> 45;
			case 46 -> 61;
			case 47 -> 91;
			case 48 -> 93;
			case 49 -> 92;
			case 51 -> 59;
			case 52 -> 39;
			case 53 -> 96;
			case 54 -> 44;
			case 55 -> 46;
			case 56 -> 47;
			case 57 -> 280;
			case 70 -> 283;
			case 71 -> 281;
			case 72 -> 284;
			case 73 -> 260;
			case 74 -> 268;
			case 75 -> 266;
			case 76 -> 261;
			case 77 -> 269;
			case 78 -> 267;
			case 79 -> 262;
			case 80 -> 263;
			case 81 -> 264;
			case 82 -> 265;
			case 83 -> 282;
			case 84 -> 331;
			case 85 -> 332;
			case 86 -> 333;
			case 87 -> 334;
			case 88 -> 335;
			case 98 -> 320;
			case 99 -> 330;
			case 103 -> 336;
			case 224 -> 341;
			case 225 -> 340;
			case 226 -> 342;
			case 227 -> 343;
			case 228 -> 345;
			case 229 -> 344;
			case 230 -> 346;
			case 231 -> 347;
			default -> sdlScancode > 0 && sdlScancode <= MAX_SDL_SCANCODE
				? SYNTHETIC_KEY_BASE + sdlScancode
				: -1;
		};
	}

	public static int toGlfwKeycode(int sdlKeycode, int fallbackScancode) {
		if (sdlKeycode >= 97 && sdlKeycode <= 122) return 65 + (sdlKeycode - 97);
		if (sdlKeycode >= 48 && sdlKeycode <= 57) return sdlKeycode;
		int mapped = switch (sdlKeycode) {
			case 8 -> 259;
			case 9 -> 258;
			case 13 -> 257;
			case 27 -> 256;
			case 127 -> 261;
			case 1073741903 -> 262;
			case 1073741904 -> 263;
			case 1073741905 -> 264;
			case 1073741906 -> 265;
			case 1073741897 -> 260;
			case 1073741898 -> 268;
			case 1073741901 -> 269;
			case 1073741899 -> 266;
			case 1073741902 -> 267;
			case 1073742048 -> 341;
			case 1073742052 -> 345;
			case 1073742049 -> 340;
			case 1073742053 -> 344;
			case 1073742050 -> 342;
			case 1073742054 -> 346;
			case 1073742051 -> 343;
			case 1073742055 -> 347;
			default -> -1;
		};
		return mapped >= 0 ? mapped : toGlfwKey(fallbackScancode);
	}

	public static int toSdlScancode(int glfwKey) {
		if (glfwKey > SYNTHETIC_KEY_BASE && glfwKey <= SYNTHETIC_KEY_BASE + MAX_SDL_SCANCODE) {
			return glfwKey - SYNTHETIC_KEY_BASE;
		}
		if (glfwKey >= 65 && glfwKey <= 90) return 4 + (glfwKey - 65);
		if (glfwKey >= 49 && glfwKey <= 57) return 30 + (glfwKey - 49);
		if (glfwKey == 48) return 39;
		if (glfwKey >= 290 && glfwKey <= 301) return 58 + (glfwKey - 290);
		if (glfwKey >= 302 && glfwKey <= 313) return 104 + (glfwKey - 302);
		if (glfwKey >= 321 && glfwKey <= 329) return 89 + (glfwKey - 321);

		return switch (glfwKey) {
			case 257 -> 40;
			case 256 -> 41;
			case 259 -> 42;
			case 258 -> 43;
			case 32 -> 44;
			case 45 -> 45;
			case 61 -> 46;
			case 91 -> 47;
			case 93 -> 48;
			case 92 -> 49;
			case 59 -> 51;
			case 39 -> 52;
			case 96 -> 53;
			case 44 -> 54;
			case 46 -> 55;
			case 47 -> 56;
			case 280 -> 57;
			case 283 -> 70;
			case 281 -> 71;
			case 284 -> 72;
			case 260 -> 73;
			case 268 -> 74;
			case 266 -> 75;
			case 261 -> 76;
			case 269 -> 77;
			case 267 -> 78;
			case 262 -> 79;
			case 263 -> 80;
			case 264 -> 81;
			case 265 -> 82;
			case 282 -> 83;
			case 331 -> 84;
			case 332 -> 85;
			case 333 -> 86;
			case 334 -> 87;
			case 335 -> 88;
			case 320 -> 98;
			case 330 -> 99;
			case 336 -> 103;
			case 341 -> 224;
			case 340 -> 225;
			case 342 -> 226;
			case 343 -> 227;
			case 345 -> 228;
			case 344 -> 229;
			case 346 -> 230;
			case 347 -> 231;
			default -> -1;
		};
	}
}
