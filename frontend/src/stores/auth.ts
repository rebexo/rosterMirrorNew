import { defineStore } from 'pinia'
import axios from 'axios'
import { jwtDecode } from 'jwt-decode'

interface User {
  username: string;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    // holt nur reinen string (oder null)
    token: localStorage.getItem('authToken') || null,
    user: null as User | null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token
  },

  actions: {
    // baut status wieder auf
    initialize() {
      if (this.token) {
        // 1. Axios den Token geben
        axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`

        try {
          // 2. User entschlüsseln
          const decodedToken: { sub: string } = jwtDecode(this.token)
          this.user = { username: decodedToken.sub }
        } catch (error) {
          console.error("Fehler beim Entschlüsseln des Tokens", error)
          this.logout()
        }
      }
    },

    async login(credentials: any) {
      const response = await axios.post('/api/auth/login', credentials)
      const token = response.data.token

      this.token = token
      localStorage.setItem('authToken', token)
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

      const decodedToken: { sub: string } = jwtDecode(token)
      this.user = { username: decodedToken.sub }
    },

    async register(credentials: any) {
      if (credentials.password !== credentials.confirmPassword) {
        throw new Error("Die Passwörter stimmen nicht überein.")
      }

      const response = await axios.post('/api/auth/register', {
        username: credentials.username,
        password: credentials.password,
        confirmPassword: credentials.confirmPassword
      })

      const token = response.data.token
      this.token = token
      localStorage.setItem('authToken', token)
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

      const decodedToken: { sub: string } = jwtDecode(token)
      this.user = { username: decodedToken.sub }
    },

    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('authToken')
      delete axios.defaults.headers.common['Authorization']
    }
  }
})
