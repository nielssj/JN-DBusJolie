include "console.iol"

type concatRequest: void {
	.arg0: string
	.arg1: string
}

interface JolieToJava {
	RequestResponse:
		concat (concatRequest)(string)
}

outputPort JavaService {
	Location: "dbus:/net.jolie.test:/Test"
	Interfaces: JolieToJava
}

main
{
	arg.arg0 = "Hello";
	arg.arg1 = "World";

	concat@JavaService(arg)(response);

	println@Console(response)()
}