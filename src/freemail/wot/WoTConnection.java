/*
 * WoTConnection.java
 * This file is part of Freemail
 * Copyright (C) 2011 Martin Nyhus
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package freemail.wot;

import java.util.List;
import java.util.Set;

public interface WoTConnection {
	public List<OwnIdentity> getAllOwnIdentities();
	public Set<Identity> getAllTrustedIdentities(String trusterId);
	public Set<Identity> getAllUntrustedIdentities(String trusterId);
	public Identity getIdentity(String identity, String truster);
	public boolean setProperty(String identity, String key, String value);
	public String getProperty(String identity, String key);
}
