include "okular.iol"

outputPort Okular {
	Location: "dbus://localhost:8000"
	//Location: "dbus:SESSION:org.kde.okular-7526/okular"
	Protocol: sodep
	Interfaces: Okular
}

main
{
	nextPage@Okular()( response )
}
