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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/*
 * AuthListener.java
 * Purpose: To listen for any events that pertain to this plugin
 * 
 * @author Brandon Dibble (aka QuantumDev)
 * @version 1.0 9/26/12
 */
public class AuthListener implements Listener {

	private AuthAhead plugin;
	
	/*
	 * Creates the listener
	 */
	public AuthListener(AuthAhead instance) {
		plugin = instance;
	}
	
	/*
	 * When the player does (almost) anything, if the server isn't
	 * on online-mode and if they aren't logged in, send them a
	 * message and cancel the event
	 * 
	 * @param event The PlayerInteractEvent
	 */
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!plugin.getServer().getOnlineMode()) {
			if (!plugin.loggedIn.contains(player.getName().toLowerCase())) {
				player.sendMessage("You must be logged in before you can play!");
				event.setCancelled(true);
			}
		}
	}
	
	/*
	 * When a player joins, if they aren't registered and the server
	 * is on offline-mode, that player will be kicked for not being
	 * registered. If the player is registered, it will tell them to
	 * login
	 * 
	 * @param event The PlayerJoinEvent
	 */
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!plugin.logins.containsKey(player.getName().toLowerCase()) && !plugin.getServer().getOnlineMode())
			player.kickPlayer("You're not registered");
		if (!plugin.getServer().getOnlineMode()) {
			for (Player p : plugin.getServer().getOnlinePlayers())
				if (p.getName().equals(player.getName()))
					player.kickPlayer("Same username");
		}
		player.sendMessage("Please login to your offline-mode account to play!");
	}
	
	/*
	 * Every time a player moves, it checks if the server is on
	 * offline-mode, then checks an sees if they aren't logged in
	 * and if they aren't it cancels that event
	 * 
	 * @param event The PlayerMoveEvent
	 */
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!plugin.getServer().getOnlineMode()) {
			if (!plugin.loggedIn.contains(player.getName().toLowerCase())) {
				event.setCancelled(true);
			}
		}
	}
	
	/*
	 * Whenever a player places a block, this method will check
	 * if the server is on offline-mode, then checks if that
	 * player isn't logged in. If they aren't, it will send them
	 * a message and cancels that event
	 * 
	 * @param event The BlockPlaceEvent
	 */
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (!plugin.getServer().getOnlineMode()) {
			if (!plugin.loggedIn.contains(player.getName().toLowerCase())) {
				player.sendMessage("You must be logged in before you can place blocks!");
				event.setCancelled(true);
			}
		}
	}
	
	/*
	 * Whenever a block is broken, it sees if the server is on
	 * offline-mode and if they aren't logged in. If they aren't,
	 * this method sends a message and cancels the event
	 * 
	 * @param event The BlockBreakEvent
	 */
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (!plugin.getServer().getOnlineMode()) {
			if (!plugin.loggedIn.contains(player.getName().toLowerCase())) {
				player.sendMessage("You must be logged in before you can break blocks!");
				event.setCancelled(true);
			}
		}
	}	
	
	/*
	 * This method handles all player commands
	 * 
	 * @param event The PlayerCommandPreprocessEvent
	 */
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		String[] args = event.getMessage().split(" ");
		Player player = event.getPlayer();
		if (args[0].equalsIgnoreCase("/register")) {
			if (plugin.getServer().getOnlineMode()) { //If the server is on online-mode
				if (args.length >= 2) { //If the command has at least 2 arguments
					if (!plugin.logins.containsKey(player.getName().toLowerCase())) { //Checks if the player is already registered
						plugin.logins.put(player.getName().toLowerCase(), sha1(args[1]));
						player.sendMessage("You successfully registered!");
					} else { //If the player is already registered
						player.sendMessage("You're already registered!");
					}
				} else { //If the command only has 1 argument (/register)
					player.sendMessage("Usage: /register <password>");
				}
			} else { //If the server is on offline-mode
				player.sendMessage("Sorry, you can only do this when the server is in online-mode.");
			}
		} else if (args[0].equalsIgnoreCase("/login")) {
			if (!plugin.getServer().getOnlineMode()) { //If the server is on offline-mode
				if (args.length >= 2) { //If the commands has at least 2 arguments
					if (plugin.logins.containsKey(player.getName().toLowerCase())) { //If the player is registered
						if (!plugin.loggedIn.contains(player.getName().toLowerCase())) { //If the player isn't already logged in
							if (plugin.logins.get(player.getName().toLowerCase()).equals(sha1(args[1]))) { //If the password matches the one in the map
								plugin.loggedIn.add(player.getName().toLowerCase());
								player.sendMessage("You have successfully logged in!");
							} else { //Incorrect password
								player.sendMessage("You have entered an incorrect password!");
							}
						} else { //Already logged in
							player.sendMessage("You're already logged in!");
						}
					} else { //They aren't registered
						player.sendMessage("You have to be registered in order to login!");
					}
				} else { //Doesn't have the proper amount of arguments
					player.sendMessage("Usage: /login <password>");
				}
			} else { //If the server is on online-mode
				player.sendMessage("You can only login when the server is in offline-mode.");
			}
		} else if (args[0].equalsIgnoreCase("/changepassword")) {
			if (plugin.getServer().getOnlineMode()) { //If the server is on online-mode
				if (args.length >= 3) { //If the command has at least 3 arguments
					if (plugin.logins.containsKey(player.getName().toLowerCase())) { //If the player is already registered
						if (plugin.logins.get(player.getName().toLowerCase()).equals(sha1(args[1]))) { //If the old password matches the one in the map
							plugin.logins.remove(player.getName().toLowerCase());
							plugin.logins.put(player.getName().toLowerCase(), sha1(args[2]));
							player.sendMessage("Your password has been changed.");
						} else { //Incorrect password
							player.sendMessage("You've entered an incorrect password.");
						}
					} else { //If the player isn't registered
						player.sendMessage("You have to be registered in order to change your password.");
					}
				} else { //If there aren't at least 3 arguments
					player.sendMessage("Usage: /changepassword <old password> <new password>");
				}
			} else { //If the server is in offline-mode
				player.sendMessage("Sorry, you can only do this when the server is in online-mode.");
			}
		} else if (args[0].equalsIgnoreCase("/resetpassword")) {
			if (args.length >= 3) { //If the command has at least 3 arguments
				if (plugin.logins.containsKey(args[1].toLowerCase())) { //If the player entered exists in the map
					plugin.logins.remove(args[1].toLowerCase());
					plugin.logins.put(args[1].toLowerCase(), sha1(args[2]));
					player.sendMessage(args[1] + "'s password has been changed.");
				} else { //The player doesn't exist in the map
					player.sendMessage("That user doesn't seem to be registered.");
				}
			} else { //If the command had less than 3 arguments
				player.sendMessage("Usage: /resetpassword <player> <new password>");
			}
		}
	}
	
	/*
	 * This function encrypts a script into SHA1 by first turning 
	 * the input string into a byte array, then runs it through a
	 * digest (SHA1). After that, it turns the byte array into a
	 * hex string
	 * 
	 * @param a The input string that is going to be encypted
	 * 
	 * @return It returns the SHA1 encrypted string
	 */
	public String sha1(String a) {
		MessageDigest md = null;
	    try {
	        md = MessageDigest.getInstance("SHA1");
	    }
	    catch(NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    byte[] convert = md.digest(a.getBytes());
	    String result = "";
	    for (int i = 0; i < convert.length; i++)
	    	result += Integer.toString((convert[i] & 0xff) + 0x100, 16).substring(1);
		return result;
	}
	
}
