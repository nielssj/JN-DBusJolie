/***************************************************************************
 *   Copyright (C) by Fabrizio Montesi                                     *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Library General Public License as       *
 *   published by the Free Software Foundation; either version 2 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU Library General Public     *
 *   License along with this program; if not, write to the                 *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 *                                                                         *
 *   For details about the authors of this software, see the AUTHORS file. *
 ***************************************************************************/
package jolie.net;

import jolie.net.ports.OutputPort;
import cx.ath.matthew.unix.UnixSocket;
import cx.ath.matthew.unix.UnixSocketAddress;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import jolie.net.ext.CommChannelFactory;
import jolie.runtime.AndJarDeps;

@AndJarDeps({"unix.jar"})
public class DBusCommChannelFactory extends CommChannelFactory
{
	public DBusCommChannelFactory( CommCore commCore )
	{
		super( commCore );
	}

	public CommChannel createChannel( URI location, OutputPort port )
		throws IOException
	{
		CommChannel ret = null;
		
                try {
                    ret = new DBusCommChannel(location, port.getProtocol());
                }
                catch (URISyntaxException ex)
                {
                    System.out.println("Failed to create D-Bus communication channel");
                }
                                
		return ret;
	}
}
