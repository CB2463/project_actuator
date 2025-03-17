package com.example.server_actautor

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.net.Socket
import java.io.*



class MainActivity : AppCompatActivity() {
    private lateinit var TEXTPOWER: TextView
    private lateinit var BUTTONON: Button
    private lateinit var BUTTONOFF: Button
    private lateinit var BUTTONOPEN: Button
    private lateinit var BUTTONCLOSE: Button
    private lateinit var SOCKETOFF: Button

    private var socket: Socket? = null
    private var output: PrintWriter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        TEXTPOWER = findViewById(R.id.TEXT_POWER)
        BUTTONON = findViewById(R.id.BUTTON_ON)
        BUTTONOFF = findViewById(R.id.BUTTON_OFF)
        BUTTONOPEN = findViewById(R.id.BUTTON_OPEN)
        BUTTONCLOSE = findViewById(R.id.BUTTON_CLOSE)
        SOCKETOFF = findViewById(R.id.SOCKET_OFF)


        CoroutineScope(Dispatchers.IO).launch {
            connectToServer("192.168.0.234", 8080)  // 서버 IP와 포트 입력
        }

        BUTTONON.setOnClickListener {
            TEXTPOWER.text = "ON"
            sendMessageToServer("on")
        }
        BUTTONOFF.setOnClickListener {
            TEXTPOWER.text = "OFF"
            sendMessageToServer("off")
        }
        BUTTONOPEN.setOnClickListener {
            sendMessageToServer("open")
        }
        BUTTONCLOSE.setOnClickListener {
            sendMessageToServer("close")
        }
        SOCKETOFF.setOnClickListener {
            sendMessageToServer("skoff")
        }

    }

    private fun connectToServer(serverIp: String, serverPort: Int) {

        Thread {
            try {
                socket = Socket(serverIp, serverPort) // 서버와 연결
                output = PrintWriter(OutputStreamWriter(socket!!.getOutputStream()), true)
                runOnUiThread {
                    Toast.makeText(this, "서버 연결 성공", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun sendMessageToServer(message: String) {
        Thread {
            try {
                output?.println(message) // 서버에 메시지 전송
                runOnUiThread {
                    Toast.makeText(this, "보낸 메시지: $message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "메시지 전송 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        socket?.close() // 앱 종료 시 소켓 닫기
    }
}