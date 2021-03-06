/***************************************************************************
 *   Copyright (C) 2009 by Fabrizio Montesi <famontesi@gmail.com>          *
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

include "TestUnit.iol"
include "console.iol"
include "file.iol"
include "runtime.iol"

outputPort TestUnit {
Interfaces: TestUnitInterface
}

init
{
	dirs[0] = "primitives";
	dirs[1] = "library"
}

main
{
	loadRequest.type = "Jolie";
	listRequest.regex = ".*\\.ol";
	listRequest.directory -> dirs[i];
	for( i = 0, i < #dirs, i++ ) {
		list@File( listRequest )( list );
		for( k = 0, k < #list.result, k++ ) {
			print@Console( list.result[k] + "\t...\t" )();
			loadRequest.filepath = listRequest.directory + "/" + list.result[k];
			scope( s ) {
				install( RuntimeException => println@Console( s.RuntimeException.stackTrace )() );
				loadEmbeddedService@Runtime( loadRequest )( TestUnit.location );
				install( TestFailed => println@Console( "failed. " + s.TestFailed )() );
				test@TestUnit()();
				println@Console( "passed." )();
				callExit@Runtime( TestUnit.location )()
			}
		}
	}
}
