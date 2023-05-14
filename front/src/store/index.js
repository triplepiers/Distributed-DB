import { createStore } from "vuex"

const actions = {

}

const mutations = {

}

const state = {
    tables: [
        {"name":"passwords","writable":true},
        {"name":"users","writable":true},
        {"name":"t2-1","writable":false}
    ]
}

export default createStore({
    actions,
    mutations,
    state
})