import { createStore } from 'vuex'

export default createStore({
  state: {
    token: sessionStorage.getItem('token') || '',
    user: (() => { try { return JSON.parse(sessionStorage.getItem('user') || 'null') } catch { return null } })(),
  },
  getters: {
    isLoggedIn: state => !!state.token,
    user: state => state.user,
    token: state => state.token,
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      sessionStorage.setItem('token', token)
    },
    SET_USER(state, user) {
      state.user = user
      sessionStorage.setItem('user', JSON.stringify(user))
    },
    LOGOUT(state) {
      state.token = ''
      state.user = null
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      sessionStorage.removeItem('csrfToken')
    }
  },
  actions: {
    login({ commit }, { token, user }) {
      commit('SET_TOKEN', token)
      commit('SET_USER', user)
    },
    logout({ commit }) {
      commit('LOGOUT')
    }
  }
})
