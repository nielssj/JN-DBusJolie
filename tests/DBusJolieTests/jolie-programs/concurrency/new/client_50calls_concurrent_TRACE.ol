include "console.iol"
include "time.iol"
include "TwiceInterface.iol"

outputPort TwiceService {
	Location: "dbus:/org.testname:/object"
	Interfaces: TwiceInterface
}

main
{
	// Initialize array to all 0's
	for ( i = 0, i < 50, i++) {
	  response[i] = 0
	};
	
	
	// FOLLOWING GENERATED
	// .. using this JavaScript doodle:
	/*  
	    output = "";
	    for(var i = 0; i < 50; i++) {
	      output += "twice@TwiceService( " + i + ")( response[" + i + "]) |\n";
	    }
	    console.log(output);
	*/
	
	twice@TwiceService( 0)( response[0]) |
	twice@TwiceService( 1)( response[1]) |
	twice@TwiceService( 2)( response[2]) |
	twice@TwiceService( 3)( response[3]) |
	twice@TwiceService( 4)( response[4]) |
	twice@TwiceService( 5)( response[5]) |
	twice@TwiceService( 6)( response[6]) |
	twice@TwiceService( 7)( response[7]) |
	twice@TwiceService( 8)( response[8]) |
	twice@TwiceService( 9)( response[9]) |
	twice@TwiceService( 10)( response[10]) |
	twice@TwiceService( 11)( response[11]) |
	twice@TwiceService( 12)( response[12]) |
	twice@TwiceService( 13)( response[13]) |
	twice@TwiceService( 14)( response[14]) |
	twice@TwiceService( 15)( response[15]) |
	twice@TwiceService( 16)( response[16]) |
	twice@TwiceService( 17)( response[17]) |
	twice@TwiceService( 18)( response[18]) |
	twice@TwiceService( 19)( response[19]) |
	twice@TwiceService( 20)( response[20]) |
	twice@TwiceService( 21)( response[21]) |
	twice@TwiceService( 22)( response[22]) |
	twice@TwiceService( 23)( response[23]) |
	twice@TwiceService( 24)( response[24]) |
	twice@TwiceService( 25)( response[25]) |
	twice@TwiceService( 26)( response[26]) |
	twice@TwiceService( 27)( response[27]) |
	twice@TwiceService( 28)( response[28]) |
	twice@TwiceService( 29)( response[29]) |
	twice@TwiceService( 30)( response[30]) |
	twice@TwiceService( 31)( response[31]) |
	twice@TwiceService( 32)( response[32]) |
	twice@TwiceService( 33)( response[33]) |
	twice@TwiceService( 34)( response[34]) |
	twice@TwiceService( 35)( response[35]) |
	twice@TwiceService( 36)( response[36]) |
	twice@TwiceService( 37)( response[37]) |
	twice@TwiceService( 38)( response[38]) |
	twice@TwiceService( 39)( response[39]) |
	twice@TwiceService( 40)( response[40]) |
	twice@TwiceService( 41)( response[41]) |
	twice@TwiceService( 42)( response[42]) |
	twice@TwiceService( 43)( response[43]) |
	twice@TwiceService( 44)( response[44]) |
	twice@TwiceService( 45)( response[45]) |
	twice@TwiceService( 46)( response[46]) |
	twice@TwiceService( 47)( response[47]) |
	twice@TwiceService( 48)( response[48]) |
	twice@TwiceService( 49)( response[49]) |
	
	/* END OF GENERATED-CODE */
	
	
	// Trace array content as execution progresses
	// ( Refresh-rate can be adjusted to the speed of your system )
	refreshrate = 15; // ms
	for ( j = 0, j < 100, j++) {
	  print@Console("Responses: [")();
	  
	  for ( k = 0, k < 50, k++ ) {
	    print@Console(response[k] + ", ")()
	  };
	  
	  print@Console("]\n")();
	  sleep@Time(refreshrate)()
	}
}
