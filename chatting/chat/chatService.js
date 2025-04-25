// chatService.js
import db from './db.js'  

export async function saveChatToDB(message) {
  const sql = `
    INSERT INTO chat (type, \`from\`, \`to\`, content, sendTime)
    VALUES (?, ?, ?, ?, ?)
  `
  const values = [message.type, message.from, message.to, message.content, message.sendTime]
  await db.execute(sql, values)
}