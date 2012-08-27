/*
Copyright (C) 2012 Brandon Dibble (aka QuantumDev)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
IN THE SOFTWARE.
*/

package com.quantumdev.authahead;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * AuthAhead.java
 * Purpose: The class that sets up the whole plugin
 * 
 * @author Brandon Dibble (aka QuantumDev)
 * @version 1.0 9/26/12
 */
public class AuthAhead extends JavaPlugin {

	public HashMap<String, String> logins = new HashMap<String, String>();
	public List<String> loggedIn = new ArrayList<String>();
	
	private AuthListener authListener;
	
	/*
	 * When the plugin is enabled, I create and register my listeners, read
	 * the current logins file to fill my HashMap, and setup a scheduler that
	 * automatically saves the logins every 15 minutes (20 ticks * 60 seconds * 15 minutes) 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onEnable() {
		authListener = new AuthListener(this);
		getServer().getPluginManager().registerEvents(authListener, this);
		try	{		
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("plugins" + File.separator + "AuthAhead" + File.separator + "logins.bin"));
			Object result = ois.readObject();
			logins = (HashMap<String, String>) result;
		}
		catch(Exception e)
		{
			System.out.println("Could not load logins! (Could be your first load)");
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				saveLogins();
			} }, 18000L, 18000L);
	}
	
	/*
	 * On disable, just save the logins hashmap
	 */
	@Override
	public void onDisable() {
		saveLogins();
	}
	
	/*
	 * Saves all logins
	 */
	public void saveLogins() {
		try {
			File f = new File("plugins" + File.separator + "AuthAhead");
			f.mkdirs();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("plugins" + File.separator + "AuthAhead" + File.separator + "logins.bin"));
			oos.writeObject(logins);
			oos.flush();
			oos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
