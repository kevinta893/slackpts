package server;

import command.CheckCmd;
import command.CoinCmd;
import command.DebugCmd;
import command.FortuneCmd;
import command.RegisterCmd;
/*
The MIT License (MIT)

Copyright (c) 2014 Kevin Ta

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
import command.SlapCmd;
import command.TipCmd;


/**
 * Runs the server software
 * @author Kevin
 *
 */
public class Driver {
	
	private static final int DEFAULT_PORT = 48567;
	
	public static void main(String[] args){
		
		
		Server instance = new Server(DEFAULT_PORT);
		
		//register all commands
		instance.registerCommand(new SlapCmd());
		instance.registerCommand(new CheckCmd());
		instance.registerCommand(new TipCmd());
		instance.registerCommand(new RegisterCmd());
		instance.registerCommand(new DebugCmd());
		instance.registerCommand(new FortuneCmd());
		instance.registerCommand(new CoinCmd());
		//start server
		instance.startServer();
	}
}
