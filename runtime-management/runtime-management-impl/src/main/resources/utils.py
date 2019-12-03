import base64

from Cryptodome.Cipher import AES


class SymmetricEncryptor(object):
    def __init__(self, key):
        self.bs = 16
        self.iv = b'\x00' * self.bs
        self.key = key

    def encrypt(self, raw):
        raw = self.__pad(raw)
        cipher = AES.new(self.key, AES.MODE_CBC, self.iv)
        return base64.b64encode(cipher.encrypt(raw.encode()))

    def decrypt(self, enc):
        enc = base64.b64decode(enc)
        cipher = AES.new(self.key, AES.MODE_CBC, self.iv)
        return self.__un_pad(cipher.decrypt(enc))

    def __pad(self, s):
        return s + (self.bs - len(s) % self.bs) * chr(self.bs - len(s) % self.bs)

    # noinspection PyMethodMayBeStatic
    def __un_pad(self, s):
        return s[:-ord(s[len(s) - 1:])]
