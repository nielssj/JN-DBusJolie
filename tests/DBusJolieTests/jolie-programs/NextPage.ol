include "okular.iol"
include "console.iol"

outputPort Okular {
	Location: "dbus:/org.kde.okular-2611:/okular"
	Interfaces: Okular
}

main
{	
	req.params.param = 1;
	req.params[1].param = 1;
	//openDocument@Okular( "~/Downloads/dbus-java.pdf" );
	test@Okular(req );
	goToPage@Okular( 17 );
	slotNextPage@Okular()();

	currentDocument@Okular()(currentDocument);
	println@Console( "Current doc is "+ currentDocument)();


	currentPage@Okular()(currentPage);
	println@Console( "Current page is "+ currentPage)()
}