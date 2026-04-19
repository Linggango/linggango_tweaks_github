package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.mojang.blaze3d.platform.Window;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class DarkWindowBar {
    private static final Logger LOGGER = LoggerFactory.getLogger("Linggango Tweaks");

    public static void setDarkWindowBar(@NonNull Window window) {
        if (Platform.get() != Platform.WINDOWS) {
            return;
        }

        WinNT.OSVERSIONINFO osversioninfo = new WinNT.OSVERSIONINFO();
        Kernel32.INSTANCE.GetVersionEx(osversioninfo);

        if (osversioninfo.dwMajorVersion.longValue() < 10L || osversioninfo.dwBuildNumber.longValue() < 17763L) {
            return;
        }

        long glfwWindow = window.getWindow();
        long hwndLong = GLFWNativeWin32.glfwGetWin32Window(glfwWindow);

        WinDef.HWND hwnd = new WinDef.HWND(Pointer.createConstant(hwndLong));

        Memory mem = new Memory(4);
        mem.setInt(0, 1);

        DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, DwmApi.DWMWA_USE_IMMERSIVE_DARK_MODE, new WinDef.LPVOID(mem), new WinDef.DWORD(4L));

        int oldWidth = window.getScreenWidth();
        window.setWindowed(oldWidth + 1, window.getScreenHeight());
        window.setWindowed(oldWidth, window.getScreenHeight());
    }

    public interface DwmApi extends StdCallLibrary {
        DwmApi INSTANCE = Native.loadLibrary("dwmapi", DwmApi.class, W32APIOptions.DEFAULT_OPTIONS);
        WinDef.DWORD DWMWA_USE_IMMERSIVE_DARK_MODE = new WinDef.DWORD(20L);
        void DwmSetWindowAttribute(WinDef.HWND paramHWND, WinDef.DWORD paramDWORD1, WinDef.LPVOID paramLPVOID, WinDef.DWORD paramDWORD2);
    }
}