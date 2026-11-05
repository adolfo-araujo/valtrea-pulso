import { test, expect } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'
import { execSync } from 'node:child_process'

// Confirma o e-mail direto no banco do CI (o código vai por e-mail, que no CI só aparece no log da API).
const confirmarNoBanco = (email) =>
  execSync(`psql "${process.env.DATABASE_URL_PSQL}" -c "UPDATE usuarios SET email_confirmado_em = now() WHERE email = '${email}'"`)

async function semProblemasDeAcessibilidade(page, nome) {
  const { violations } = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze()
  const lista = violations.map((v) => `- ${v.id}: ${v.help} (${v.nodes.length}x) ${v.nodes[0]?.target.join(' ')}`).join('\n')
  expect(violations, `Problemas de acessibilidade em ${nome}:\n${lista}`).toEqual([])
}

// Todos os testes falham se a CSP de produção bloquear algo ou se a página tiver erro de JavaScript.
test.beforeEach(async ({ page }, testInfo) => {
  testInfo.problemas = []
  page.on('console', (msg) => {
    if (msg.type() === 'error' && /Content Security Policy|Refused to/i.test(msg.text())) testInfo.problemas.push(msg.text())
  })
  page.on('pageerror', (e) => testInfo.problemas.push(e.message))
})
test.afterEach(async ({}, testInfo) => {
  expect(testInfo.problemas, testInfo.problemas.join('\n')).toEqual([])
})

for (const [nome, rota] of [
  ['início', '/'],
  ['entrar', '/entrar'],
  ['criar conta', '/cadastro'],
  ['esqueci a senha', '/esqueci-senha'],
  ['termos', '/termos'],
  ['privacidade', '/privacidade'],
  ['acessibilidade', '/acessibilidade'],
  ['excluir conta', '/excluir-conta'],
  ['bairro inexistente', '/b/00000000-0000-0000-0000-000000000000'],
  ['iniciativa inexistente', '/i/00000000-0000-0000-0000-000000000000'],
]) {
  test(`acessibilidade: ${nome}`, async ({ page }) => {
    await page.goto(rota)
    await page.waitForLoadState('networkidle')
    await expect(page.locator('h1')).toBeVisible()
    await semProblemasDeAcessibilidade(page, nome)
  })
}

async function criarConta(page, nome, email) {
  await page.goto('/cadastro')
  await page.getByLabel('Nome').fill(nome)
  await page.getByLabel('E-mail').fill(email)
  await page.getByLabel('Senha').fill('senha-forte-123')
  await page.getByLabel(/Tenho 18 anos/).check()
  await page.getByRole('button', { name: 'Criar conta' }).click()
  await expect(page.getByRole('heading', { name: 'Confirme seu e-mail' })).toBeVisible()
  confirmarNoBanco(email)
}

test('fluxo completo: escolher o bairro, publicar, o vizinho apoia e todos veem', async ({ page, browser }, testInfo) => {
  const sufixo = `${testInfo.project.name}-${Date.now()}`
  const nomeBairro = `Vila Ensaio ${sufixo}`

  // A autora cria a conta e cadastra o bairro
  await criarConta(page, 'Dona Teste', `dona-${sufixo}@test.dev`)
  await page.goto('/painel')
  await expect(page.getByRole('heading', { name: 'Escolha o seu bairro' })).toBeVisible()
  await semProblemasDeAcessibilidade(page, 'escolher bairro')
  const cadastrar = page.getByRole('region', { name: 'Não achou? Cadastre' })
  await cadastrar.getByLabel('Bairro', { exact: true }).fill(nomeBairro)
  await cadastrar.getByLabel('Cidade').fill('Rio de Janeiro')
  await cadastrar.getByLabel('Estado').selectOption('RJ')
  await cadastrar.getByRole('button', { name: 'Cadastrar e morar aqui' }).click()
  await expect(page.getByRole('heading', { name: nomeBairro })).toBeVisible()
  await expect(page.getByText('Nenhuma iniciativa aqui ainda.')).toBeVisible()
  await semProblemasDeAcessibilidade(page, 'bairro vazio')

  // Publica uma iniciativa
  await page.getByRole('link', { name: 'Nova iniciativa' }).click()
  await page.getByLabel('Título').fill('Trocar a lâmpada do poste da esquina')
  await page.getByLabel('Categoria').selectOption('ILUMINACAO')
  await page.getByLabel('O que precisa e por quê').fill('O poste da esquina está apagado há duas semanas e a rua fica escura.')
  await semProblemasDeAcessibilidade(page, 'nova iniciativa')
  await page.getByRole('button', { name: 'Publicar' }).click()
  await expect(page.getByRole('heading', { name: 'Trocar a lâmpada do poste da esquina' })).toBeVisible()
  await expect(page.getByText('0 apoios')).toBeVisible()
  await page.getByLabel('O que aconteceu?').fill('Abri o pedido 2026-123 no 156.')
  await page.getByLabel('Fase').selectOption('EM_ANDAMENTO')
  await page.getByRole('button', { name: 'Publicar novidade' }).click()
  await expect(page.getByText('Abri o pedido 2026-123 no 156.')).toBeVisible()
  await semProblemasDeAcessibilidade(page, 'iniciativa da autora')
  const endereco = new URL(page.url()).pathname

  // O vizinho, em outra janela, apoia
  const vizinhoCtx = await browser.newContext()
  const vizinho = await vizinhoCtx.newPage()
  await criarConta(vizinho, 'Vizinho Teste', `vizinho-${sufixo}@test.dev`)
  await vizinho.goto(endereco)
  await vizinho.getByRole('button', { name: 'Apoiar' }).click()
  await expect(vizinho.getByText('1 apoio', { exact: true })).toBeVisible()
  await expect(vizinho.getByRole('button', { name: 'Retirar meu apoio' })).toBeVisible()
  await semProblemasDeAcessibilidade(vizinho, 'iniciativa de outra pessoa')
  await vizinhoCtx.close()

  // Quem não tem conta vê a iniciativa e o apoio, só com o primeiro nome da autora
  const anonimoCtx = await browser.newContext()
  const anonimo = await anonimoCtx.newPage()
  await anonimo.goto(endereco)
  await expect(anonimo.getByRole('heading', { name: 'Trocar a lâmpada do poste da esquina' })).toBeVisible()
  await expect(anonimo.getByText('1 apoio', { exact: true })).toBeVisible()
  await expect(anonimo.getByText('Por Dona,', { exact: false })).toBeVisible()
  await expect(anonimo.getByText(/Dona Teste/)).toHaveCount(0)
  await expect(anonimo.getByText('Em andamento').first()).toBeVisible()
  await anonimo.getByRole('link', { name: new RegExp(nomeBairro) }).click()
  await expect(anonimo.getByRole('heading', { name: nomeBairro })).toBeVisible()
  await expect(anonimo.getByRole('link', { name: /Trocar a lâmpada/ })).toBeVisible()
  await semProblemasDeAcessibilidade(anonimo, 'bairro com iniciativa')
  await anonimoCtx.close()
})
