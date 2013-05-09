/***************************************************************************
 *   Copyright (C) 2008-09-10 by Fabrizio Montesi <famontesi@gmail.com>    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as               *
 *   published by the Free Software Foundation; either version 2 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public             *
 *   License along with this program; if not, write to the                 *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 *                                                                         *
 *   For details about the authors of this software, see the AUTHORS file. *
 ***************************************************************************/


include "console.iol"
include "exec.iol"
include "runtime.iol"
include "string_utils.iol"
include "viewer.iol"
include "ui/swing_ui.iol"
include "okular-dbus.iol"
include "dbus.iol"

execution { sequential }

inputPort ViewerInputPort {
Location: "local"
Interfaces: ViewerInterface
}

outputPort OkularInstance {
	Location: "dbus:/org.kde.okular-8000:/okular"
	Interfaces: OkularDBusInterface
}

outputPort DBus {
	Location: "dbus:/org.freedesktop.DBus:/"
	Interfaces: DBusInterface
}

include "presenter.iol"
include "poller.iol"

define startPoller
{
	install( RuntimeException => println@Console( main.RuntimeException.stackTrace )() );
	println@Console("Poller 1")();
	pollerData.type = "Jolie";
	pollerData.filepath = "poller.ol";
	loadEmbeddedService@Runtime( pollerData )( Poller.location );
	getLocalLocation@Runtime()( pollerStartData.viewerLocation );
	pollerStartData.presenterLocation = Presenter.location;
	start@Poller( pollerStartData )
}

define initDocumentViewer
{
	o = 0;
	ListNames@DBus ( ) ( names );
	for (i = 0, i < #names.arg0, i++)
	{
		name = names.arg0[i];
		name.prefix = "org.kde.okular-";
		startsWith@StringUtils ( name ) ( swres );
		if ( swres ) {
		    ss.result[o++] = name
		}
	};
	
	if ( #ss.result < 1 ) {
		throw( CouldNotStartFault, "Could not find a running viewer" )
	};

	if ( #ss.result > 1 ) {
		range = " (1.." + (#ss.result) + ")"
	};
	
	choiceText = "Choose a viewer instance" + range;
	for( i = 0, i < #ss.result, i++ ) {
		// We display numbers starting by 1
		OkularInstance.location = "dbus:/" + ss.result[i] + ":/okular";
		currentDocument@OkularInstance ( ) ( cDoc );
		doc = cDoc;
		trim@StringUtils( doc )( doc );
		choiceText += "\n" + (i+1) + ") " + ss.result[i] + " - Currently displaying: " + doc
	};
	selected = -1;
	// registerForInput@Console()();
	while( selected < 1 || selected > #ss.result ) {
		showInputDialog@SwingUI( choiceText )( selected );
		// print@Console( "> " )();
		// in( selected );
		selected = int(selected)
	};
	selected--;

	cmdStr = "qdbus " + ss.result[selected] + " ";
	OkularInstance.location = "dbus:/" + ss.result[selected] + ":/okular"
}

init
{
	cmdStr -> global.cmdStr;

	start( startData )() {
		initDocumentViewer;
		if ( is_defined( startData.presenterLocation ) ) {
			Presenter.location = startData.presenterLocation;
			startPoller
		}
	}
}

main
{
	[ goToPage( request ) ] {
		goToPage@OkularInstance ( request.pageNumber )
	}

	[ openDocument( request ) ] {
		openDocument@OkularInstance ( request.documentUrl )
	}

	[ close( request ) ] {
		exec@Exec( cmdStr + "/MainApplication quit" )()
	}

	[ currentPage()( response ) {
		currentPage@OkularInstance ( ) ( r );
		response = r
	} ] { nullProcess }

	[ currentDocument()( response ) {
		currentDocument@OkularInstance ( ) ( r );
		response = r;
		trim@StringUtils( response )( response )
	} ] { nullProcess }
}
