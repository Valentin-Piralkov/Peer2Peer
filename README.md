# Peer2Peer
A peer to peer communication using java UDP sockets

I have implemented a class called client that uses Datagram sockets to connect with peers and chat with them.
The Client class contain two threads one for input, and one for output. This way a user can send messages, while receiving messages from others at the same time.
When the Client is created is first asks the user to imput his username. This way, when he sends messages his peers will know who has sent them. 
When the input thread is starts, it will first use a method called addNewReceiver() which ask the user for a port number and then adds a new datagram socket to the list of sockets every input thread has.

How to work with the input thread:
1. you need to write the index of the reciever, this way the program will know where to send the message.
2. you need to write your message separated from the index with space.
3. if there is no user a that index, the proram will call addNewReceiver() and you will create a new peer. 
4. After a new peer is created the program will tell you his index in the list. 
