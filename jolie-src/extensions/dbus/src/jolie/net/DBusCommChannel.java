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

import cx.ath.matthew.unix.UnixSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import jolie.Interpreter;
import jolie.net.protocols.CommProtocol;

public class DBusCommChannel extends StreamingCommChannel implements PollableCommChannel
{
	public DBusCommChannel( UnixSocket socket, URI location, CommProtocol protocol )
		throws IOException
	{
		super( location, protocol );
		
		// TODO: Implement
	}

	protected void sendImpl( CommMessage message )
		throws IOException
	{
		// TODO: Implement?
	}
	
	protected CommMessage recvImpl()
		throws IOException
	{
                // TODO: Implement?
		return null;
	}
	
	protected void closeImpl()
		throws IOException
	{
		// TODO: Implement?
	}

	public synchronized boolean isReady()
		throws IOException
	{
		// TODO: Implement?
		return true;
	}
	
	@Override
	public void disposeForInputImpl()
		throws IOException
	{
		// TODO: Implement?
		Interpreter.getInstance().commCore().registerForPolling( this );
	}
}
