# NMF Proxy

NMF Proxy is a lightweight proxy server that utilizes NanoSat MO Framework (NMF) for data conversion.
It provides a simple and efficient way to compress and decompress data streams on the fly, 
allowing the use of the NMF Framework with EnduroSat proprietary protocols. The service has two components:

# Ground Tunnel

Responsible for handling incoming/outgoing packets on the ground. Enables sending commands from NMF applications 
running on the ground to the satellite and receiving telemetry.

# Space Tunnel

Responsible for handling incoming/outgoing packets on the satellite. Exchanges information between NMF applications running in space
and different satellite modules. Sends telemetry to the ground.

