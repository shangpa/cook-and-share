import mysql from 'mysql2/promise'

const pool = mysql.createPool({
  host: 'localhost',
  user: 'root',
  password: '0922',  // ← 너가 설정한 걸로 바꿔줘
  database: 'chatdb'
})

export default pool
