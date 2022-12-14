import { IAuthInformation } from "../interfaces/dto/user-dto";


//temporarly using session but will be replaced by cookies


export function setAuthInfo(authInfo: IAuthInformation) {
    sessionStorage.setItem('authInfo', JSON.stringify(authInfo))
}

export function getAuthInfo(): IAuthInformation | null {
    const authInfo = sessionStorage.getItem('authInfo')
    return fakeAuth
    if (authInfo) {
        return JSON.parse(authInfo)
    }
    return null
}

const fakeAuth = {
    uid: 1,
    token: '0f1c163c-29c9-4296-96e9-0afb255d5f56'
}

export function isLoggedIn(): boolean {
    return getAuthInfo() !== null
}

export function logout() {
    sessionStorage.removeItem('authInfo')
}