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
	/*
	arg.field1[0] = "nok sygt";
	//arg.field1[1] = 12;
	arg.field2 = "sygt nok";
	arg.field3.values[0] = 5;
	arg.field3.values[1] = 6;


	testmethod@Okular ( arg )
	*/
	
	openDocument@Okular( "~/Downloads/dbus-java.pdf" );
	goToPage@Okular( 10 );
	currentPage@Okular( ) ( response );
	println@Console( "currentPage is " + response )();
	currentDocument@Okular( ) ( response2 );
	println@Console( "currentDocument is " + response2 )();

	close@OkularShell( ) ( response3 );
	println@Console( "response to close is " + response3 )()
}