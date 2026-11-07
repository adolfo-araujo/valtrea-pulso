import { useEffect, useState } from 'react'
import QRCode from 'qrcode'

/** Desenha o QR Code no próprio navegador (o endereço não sai para nenhum serviço de terceiros). */
export function useQrCode(url, largura = 1024) {
  const [imagem, setImagem] = useState(null)
  useEffect(() => {
    let ativo = true
    if (!url) {
      setImagem(null)
      return undefined
    }
    QRCode.toDataURL(url, { width: largura, margin: 2, errorCorrectionLevel: 'M' })
      .then((dados) => ativo && setImagem(dados))
      .catch(() => ativo && setImagem(null))
    return () => {
      ativo = false
    }
  }, [url, largura])
  return imagem
}
