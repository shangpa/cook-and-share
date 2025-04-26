// server.js (또는 chatapp.js 라고 저장해도 됨)

import express from 'express'
import http from 'http'
import { Server } from 'socket.io'
import { saveChatToDB } from './chatService.js' // DB 저장 함수

const app = express()
const server = http.createServer(app)
const io = new Server(server)

io.on('connection', (socket) => {
  console.log(`Socket connected : ${socket.id}`)

  socket.on('enter', (data) => {
    const roomData = JSON.parse(data)
    const userName = roomData.userName
    const roomName = roomData.roomName

    socket.join(roomName)
    console.log(`[user name : ${userName}] entered [room name : ${roomName}]`)

    const enterData = {
      type: 'ENTER',
      from: userName,
      to: roomName,
      content: `${userName} (님)이 방에 들어왔습니다.`,
      sendTime: Date.now(),
    }

    socket.broadcast.to(roomName).emit('update', JSON.stringify(enterData))
  })

  socket.on('left', (data) => {
    const roomData = JSON.parse(data)
    const userName = roomData.userName
    const roomName = roomData.roomName

    socket.leave(roomName)
    console.log(`[user name : ${userName}] left [room name : ${roomName}]`)

    const leftData = {
      type: 'LEFT',
      from: userName,
      to: roomName,
      content: `${userName} (님)이 방에서 나갔습니다.`,
      sendTime: Date.now(),
    }

    socket.broadcast.to(roomName).emit('update', JSON.stringify(leftData))
  })

  socket.on('newMessage', async (data) => {
    const messageData = JSON.parse(data)
    console.log(`[Room ${messageData.to}] ${messageData.from} : ${messageData.content}`)

    await saveChatToDB(messageData) // MySQL 저장
    socket.broadcast.to(messageData.to).emit('update', JSON.stringify(messageData))
  })

  socket.on('disconnect', () => {
    console.log(`Socket disconnected : ${socket.id}`)
  })
})

// 서버 실행
server.listen(3001, () => {
  console.log('Server running at http://localhost:3001')
})
