include "okular.iol"
include "console.iol"

outputPort Okular {
	Location: "dbus:/org.kde.okular-2387:/okular"
	Interfaces: Okular
}

main
{	
	openDocument@Okular( "~/Downloads/dbus-java.pdf" );
	goToPage@Okular( 14 );
	slotNextPage@Okular()();

	currentDocument@Okular()(currentDocument);
	println@Console( "Current doc is "+ currentDocument)();


	currentPage@Okular()(currentPage);
	println@Console( "Current page is "+ currentPage)()
}