dbus <- read.csv("dbus2000.csv", header=T)

attach(dbus)
sortedDbus <- sort(Marshal.with.known.signature)
sortedUnknownDbus <- sort(Marshal.with.unknown.signature)

dsd <- sd(sortedDbus)
dsd2 <- sd(sortedUnknownDbus)
dmean <- mean(sortedDbus)
dmean2 <- mean(sortedUnknownDbus)
plot(sortedDbus, dnorm(sortedDbus, mean=dmean,sd=dsd), ylim=c(0,3), xlim=c(0,2),type="l", col="red",ann=F)
lines(sortedUnknownDbus, dnorm(sortedUnknownDbus,mean=dmean2,sd=dsd2),type="l", col="blue")

abline(v=dmean2-3*dsd2, col=rgb(0,0,1, 0.3), lty=3)
abline(v=dmean2, col=rgb(0,0,1, 0.3), lty=3)
abline(v=dmean, col=rgb(1,0,0,0.5), lty=3)
abline(v=dmean+3*dsd, col=rgb(1,0,0,0.5), lty=3)
title(main="Marshalling with known and unknown signatures, 2000 ints, 100.000 runs", xlab="ms")

text(.73,2.7, expression("m"["D-Bus known sig."]))
text(1.5, 1.5, expression("m"["D-Bus unknown sig."]))
text(.8, 2.35, expression("3 SD"["D-Bus known sig."]))
text(0.8,1.4, expression("3 SD"["D-Bus unknown sig."]))

dy <- 2.25
sy <- 1.3
arrows(dmean2, sy, x1=dmean2-3*dsd2, sy, code=3, angle=24, length=0.1, col=rgb(0,0,1, 0.6))
arrows(dmean, dy, x1=dmean+3*dsd, dy, code=3, angle=24, length=0.06, col=rgb(1,0,0, 0.6))

legend(1.2,3, 
  c(
    paste("D-Bus, Known sig.,     Mean: ",format(dmean, digits=2), " SD: ", format(dsd, digits=2)),
    paste("D-Bus, Unknown sig., Mean: ",format(dmean2, digits=4)," SD: ",format(dsd2,  digits=3))
  ), col=c("red","blue"), pch="l", cex=0.7)
