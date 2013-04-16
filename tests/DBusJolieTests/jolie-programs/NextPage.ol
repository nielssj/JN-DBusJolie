include "okular.iol"
include "console.iol"

outputPort Okular {
	Location: "dbus:/org.kde.okular-3130:/okular"
	Interfaces: Okular
}

outputPort OkularShell {
	Location: "dbus:/org.kde.okular-3130:/okular/okular__Shell"
	Interfaces: Okular__shell
}

outputPort Hello {
	Location: "dbus:/org.testname:/"
	Interfaces: HelloServer
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




	arg.params[0].field1 = "~/Downloads/dbus-java.pdf";
	arg.params[0].field2 = "lol";
	Map@Hello(arg)()

	testmethod2@Okular ( arg2 )

	
	
	openDocument@Okular( "~/Downloads/dbus-java.pdf" );

	goToPage@Okular( 10 );*/
	currentPage@Okular( ) ( response );
	println@Console( "currentPage is " + response )()
	/*currentDocument@Okular( ) ( response2 );
	println@Console( "currentDocument is " + response2 )();

	close@OkularShell( ) ( response3 );
	println@Console( "response to close is " + response3 )()
	


<<<<<<< HEAD
	arg.params[0].field1[0] = "field1.1";
	arg.params[0].field1[1] = "field1.2";
	arg.params[0].field2[0] = "field2.1";
	arg.params[0].field2[1] = "field2.2";

	intm@Hello(arg)(resp);

	// Recieve a map
	hello[0] = 0;
	hello[1] = 1;
	test@Hello(hello)(response);
	println@Console( "response to test is " + response.params[0].field1[0] )();
	println@Console( "response to test is " + response.params[0].field1[1] )();
	println@Console( "response to test is " + response.params[0].field2[0] )();
	println@Console( "response to test is " + response.params[0].field2[1] )() 
*/

	// Recieve a array
	hello[0] = 0;
	hello[1] = 1;
	test@Hello(hello)(response);
	println@Console( "response to test is " + response.params[0] )();
	println@Console( "response to test is " + response.params[1] )()
	/*arg.params[0] = 12;
	arg.params[1] = "string";
	arg.params[2].field1[0] = 11;
	arg.params[2].field2[0] = 1;
	intm@Hello(arg)(response);
	println@Console( "response to test is " + response )()*/
}