package com.centraltrillion.worklink.utils;

import android.content.Context;

import com.centraltrillion.worklink.utils.im.IO;
import com.centraltrillion.worklink.utils.im.Socket;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.SocketIOException;

import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 *
 * Use for Instant Message(IM) socket communication functionality,
 * ex: Register/UnRegister callback event, connect/disconnect and setup parameters
 * for socket io.
 * Developer should setup the dependency in build.grandle as below when use this class:
 * <pre>
 * dependencies {
 *    ...
 *    compile 'com.github.nkzawa:socket.io-client:0.3.0'
 *    ...
 * }
 * </pre>
 */
public class MessageDispatcher {
    private Context mContext = null;
    private Socket mSocket = null;

    protected MessageDispatcher() {
    }

    protected MessageDispatcher(Context context, String mServerLoc, IO.Options ioOptions) throws IllegalArgumentException {
        try {
            mContext = context;
            mSocket = IO.socket(mServerLoc, ioOptions);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URI's syntax is error for server's location");
        }
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    /**
     * Register callback event called after emitting a data to server.
     *
     * @param eventName Callback name for identifying which callback should be called.
     * @param eventCallback Callback will be called when server response for post handling.
     */
    public void registerEvent(String eventName, Emitter.Listener eventCallback) {
        mSocket.on(eventName, eventCallback);
    }

    /* TODO: Need to check for multi-session. For now, it only support single session*/
    public void unRegisterEvent(String eventName, Emitter.Listener eventCallback) {
        mSocket.off(eventName, eventCallback);
    }

    /**
     * Let socket io to connect to server
     */
    public void connect() {
        mSocket.connect();
    }

    /**
     * Let socket io to disconnect from server
     */
    public void disconnect() {
        mSocket.disconnect();
    }

    /* TODO: Maybe we need to check the socket's connection status. */
    /**
     *
     * Emit a data to Server for handling.
     *
     * @param eventName Event name to identify which callback should be called after server response.
     * @param data The data to pass to server.
     * @throws com.github.nkzawa.socketio.client.SocketIOException It's used to handle connection status checking. If socket is disconnected and you want
     * to emit a event, then it will throw a exception.
     */
    public void emitEvent(String eventName, JSONObject data) throws SocketIOException {
        /* For error handle to ensure the socket must been connected. */
//        if(!mSocket.connected()) {
//            throw new Exception("Socket not been connected");
//        }

        mSocket.emit(eventName, data);
    }


    /**
     *  Socket setup builder for parameters setup.
     */
    public static class Builder {
        private Context mContext = null;
        private IO.Options mIoOpts = null;
        private String mServerLoc = null;

        public Builder() {
        }

        public Builder(Context context) {
            mContext = context;
            mIoOpts = new IO.Options();

              /* Assign the default. */
            mIoOpts.reconnection = true;
            mIoOpts.reconnectionDelay = 1000;
            mIoOpts.timeout = 10000;
        }

        /**
         *
         * Create the MessageDispatcher instance and setup server parameters.
         *
         * @throws com.github.nkzawa.socketio.client.SocketIOException If one of Context, IO.Options and Server Location is null. Or, the
         *         server location string is empty.
         */
        public MessageDispatcher create() throws SocketIOException {
              /* Error handling for required parameters. */
            if (mContext == null
                    || mIoOpts == null
                    || (mServerLoc == null)) {
                throw new SocketIOException("One of the parameters for context, IO.Options or server's location is not been setup");
            } else if (mServerLoc.isEmpty()) {
                throw new SocketIOException("Server's location is empty string.");
            }

            return new MessageDispatcher(mContext, mServerLoc, mIoOpts);

        }

        /**
         *
         * Assign the server location.
         *
         * @param serverLoc Server location string, ex: http://54.178.163.218
         * @return
         */
        public Builder server(String serverLoc) {
            mServerLoc = serverLoc;

            return this;
        }

        /**
         *
         * Setup the flag let socket io is need to reconnect or not.
         *
         * @param isReconnect
         * @return
         */
        public Builder isReconnet(boolean isReconnect) {
            mIoOpts.reconnection = isReconnect;

            return this;
        }

        /**
         *
         *  Setup the connection timeout.
         *
         * @param reconnectDelay
         * @return
         */
        public Builder reconnectDelay(int reconnectDelay) {
            mIoOpts.reconnectionDelay = reconnectDelay;

            return this;
        }

        /**
         *
         * Setup the connection timeout.
         *
         * @param timeout
         * @return
         */
        public Builder timeout(int timeout) {
            mIoOpts.timeout = timeout;

            return this;
        }

        /**
         *
         * @param query
         * @return
         */
        public Builder query(String query) {
            mIoOpts.query = query;

            return this;
        }
    }
}
