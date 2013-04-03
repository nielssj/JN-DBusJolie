include "okular.iol"
include "console.iol"

outputPort Okular {
	Location: "dbus:/org.kde.okular-8159:/okular"
	Interfaces: Okular
}

outputPort OkularShell {
	Location: "dbus:/org.kde.okular-8159:/okular/okular__Shell"
	Interfaces: Okular__shell
}

main
{
	openDocument@Okular( "~/Downloads/dbus-java.pdf" );
	goToPage@Okular( 10 );
	currentPage@Okular( ) ( response );
	println@Console( "currentPage is " + response )();
	currentDocument@Okular( ) ( response2 );
	println@Console( "currentDocument is " + response2 )();

	close@OkularShell( ) ( response3 );
	println@Console( "response to close is " + response3 )()
}