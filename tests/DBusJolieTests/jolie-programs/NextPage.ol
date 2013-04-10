include "okular.iol"
include "console.iol"

outputPort Okular {
	//Location: "dbus:/org.kde.okular-6662:/okular"
	Location: "dbus:/org.testname:/okular"
	Interfaces: Okular
}

outputPort OkularShell {
	Location: "dbus:/org.kde.okular-6359:/okular/okular__Shell"
	Interfaces: Okular__shell
}

main
{
	/*
	arg1 = 12;

	testmethod@Okular ( arg1 );

	ar.f1 = "one";
	ar.f2 = "two";

	arg2.params[0] = 12;
	arg2.params[1] = "Doe";
	arg2.params[2].field = "fieldiamthevalueoffield";
	arg2.params[2].field2 = "thevalueoffield2iam";
	//arg2.params[2].field[1] = "field22";
	//arg2.params[3].arr[0] = 0;
	//arg2.params[3].arr[1] = 1;


	testmethod2@Okular ( arg2 )
*/
	
	
	//openDocument@Okular( "~/Downloads/dbus-java.pdf" );

	arg.params[0].field1 = "~/Downloads/dbus-java.pdf";
	arg.params[0].field2 = "lol";
	testmethod2@Okular( arg );
	goToPage@Okular( 10 );
	currentPage@Okular( ) ( response );
	println@Console( "currentPage is " + response )();
	currentDocument@Okular( ) ( response2 );
	println@Console( "currentDocument is " + response2 )();

	close@OkularShell( ) ( response3 );
	println@Console( "response to close is " + response3 )()
	
}